/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

import static br.ufmt.jedigpu.GPU.convertSMVer2Cores;
import static br.ufmt.jedigpu.GPU.createString;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jcuda.driver.JCudaDriver.*;
import static jcuda.driver.CUdevice_attribute.*;

import jcuda.*;
import jcuda.driver.*;

/**
 *
 * @author raphael
 */
public class JSeriesCUDA extends GPU {

    private int[] threadsPerBlock = new int[]{1, 1, 1};
    private int[] blocksPerGrid = new int[]{1, 1, 1};
    private String compileOptions = " -use_fast_math ";
    private String pathNvcc = "/usr/local/cuda/bin/";
    private int usedSharedMemory = 0;
    private static List<Device> devices;
    private static HashMap<String, ComputeCapability> gpuData = new HashMap<String, ComputeCapability>();

    static {
        ComputeCapability cc;
        cc = new ComputeCapability("1.0", "sm_10", 32, 24, 768, 8, 16384, 8192, 256, "block", 124, 512, 2, 512, 16384);
        gpuData.put(cc.getComputeCapability(), cc);

        cc = new ComputeCapability("1.1", "sm_11", 32, 24, 768, 8, 16384, 8192, 256, "block", 124, 512, 2, 512, 16384);
        gpuData.put(cc.getComputeCapability(), cc);

        cc = new ComputeCapability("1.2", "sm_12", 32, 32, 1024, 8, 16384, 16384, 512, "block", 124, 512, 2, 512, 16384);
        gpuData.put(cc.getComputeCapability(), cc);

        cc = new ComputeCapability("1.3", "sm_13", 32, 32, 1024, 8, 16384, 16384, 512, "block", 124, 512, 2, 512, 16384);
        gpuData.put(cc.getComputeCapability(), cc);

        cc = new ComputeCapability("2.0", "sm_20", 32, 48, 1536, 8, 49152, 32768, 128, "warp", 63, 128, 2, 1024, 49152);
        gpuData.put(cc.getComputeCapability(), cc);

        cc = new ComputeCapability("2.1", "sm_21", 32, 48, 1536, 8, 49152, 32768, 128, "warp", 63, 128, 2, 1024, 49152);
        gpuData.put(cc.getComputeCapability(), cc);

        cc = new ComputeCapability("3.0", "sm_30", 32, 64, 2048, 16, 49152, 65536, 256, "warp", 63, 256, 4, 1024, 49152);
        gpuData.put(cc.getComputeCapability(), cc);

        cc = new ComputeCapability("3.5", "sm_35", 32, 64, 2048, 16, 49152, 65536, 256, "warp", 255, 256, 4, 1024, 49152);

        gpuData.put(cc.getComputeCapability(), cc);

        JCudaDriver.setExceptionsEnabled(true);
        cuInit(0);
        int[] count = new int[1];
        cuDeviceGetCount(count);
        devices = new ArrayList<Device>();

        // Initialize the driver and create a context for the first device.
        CUdevice device;
        for (int i = 0; i < count[0]; i++) {
            device = new CUdevice();
            cuDeviceGet(device, i);
            // Obtain the device name
            byte deviceName[] = new byte[1024];
            cuDeviceGetName(deviceName, deviceName.length, device);
            String name = createString(deviceName);

            // Obtain the compute capability
            int majorArray[] = {0};
            int minorArray[] = {0};
            cuDeviceComputeCapability(majorArray, minorArray, device);
            int major = majorArray[0];
            int minor = minorArray[0];
            int array[] = {0};
            cuDeviceGetAttribute(array, CU_DEVICE_ATTRIBUTE_MULTIPROCESSOR_COUNT, device);
            int numberCores = convertSMVer2Cores(major, minor);

            devices.add(new Device(name, numberCores * array[0], device));
        }
    }

    public void execute(List<ParameterGPU> parametros, String arquivo, String metodo) throws IOException {

        if (measure) {
            allTimes = measures.get(EnumMeasure.ALL_TIME);
            if (allTimes == null) {
                allTimes = new MeasureTimeGPU();
                allTimes.setDescription("Tempo de execução total");
                measures.put(EnumMeasure.ALL_TIME, allTimes);
            } else {
                time.sum();
            }
            allTimes.setBeginLong(System.nanoTime());
            allTimes.setBegin(new Date());

            time = measures.get(EnumMeasure.LOAD_BALANCE_TIME);
            if (time == null) {
                time = new MeasureTimeGPU();
                time.setDescription("Tempo de execução do load balance");
                measures.put(EnumMeasure.LOAD_BALANCE_TIME, time);
            } else {
                time.sum();
            }
            time.setBeginLong(System.nanoTime());
            time.setBegin(new Date());
        }

        BufferedReader bur = new BufferedReader(new FileReader(arquivo));

        StringBuilder sourceBuilder = new StringBuilder();
        String line = bur.readLine();
        while (line != null) {
            sourceBuilder.append(line).append("\n");
            line = bur.readLine();
        }
        bur.close();
        File file = new File(arquivo);
        String source = sourceBuilder.toString();
        boolean hasSizeof = source.contains("#SIZEOF");

        if (devices.size() > 1) {
            int minCores = devices.get(0).getCores();

            List<List<ParameterGPU>> parametrosByGPU = new ArrayList<List<ParameterGPU>>();

            parametrosByGPU.add(new ArrayList<ParameterGPU>());

            for (int i = 1; i < devices.size(); i++) {
                if (devices.get(i).getCores() < minCores) {
                    minCores = devices.get(i).getCores();
                }
                parametrosByGPU.add(new ArrayList<ParameterGPU>());
            }
            int[] proporcao = new int[devices.size()];
            int sum = 0;

            List<String> sources = null;

            if (hasSizeof) {
                sources = new ArrayList<String>();
            }

            for (int i = 0; i < devices.size(); i++) {
                proporcao[i] = (int) Math.round(devices.get(i).getCores() / (float) minCores);
                sum += proporcao[i];
                if (hasSizeof) {
                    sources.add(new String(source));
                }
            }

            ParameterGPU parametro = null;
            for (int i = 0; i < parametros.size(); i++) {
                parametro = parametros.get(i);

                long size = parametro.getSize();
                int step = (int) size / sum;

                int begin = 0;
                int end;

                for (int j = 0; j < proporcao.length; j++) {
                    if (parametro.isDivide()) {
                        if (j < proporcao.length - 1) {
                            end = begin + step * proporcao[j];
                        } else {
                            end = (int) (size);
                        }
                        parametrosByGPU.get(j).add(parametro.cloneIndex(begin, end));
                        begin = end;
                    } else {
                        parametrosByGPU.get(j).add(parametro);
                    }
                    if (hasSizeof) {
                        sources.set(j, sources.get(j).replace("#SIZEOF" + i + "#", "" + parametrosByGPU.get(j).get(i).getSize()));
                    }
                }
            }

            ExecuteCUDA executeCUDA;
            Thread[] threads = new Thread[devices.size()];
            for (int i = 0; i < devices.size(); i++) {
                System.out.println("Thread:" + i);

                if (hasSizeof) {
                    arquivo = file.getParent() + "code" + i + ".cu";
                    PrintWriter pw = new PrintWriter(arquivo);
                    pw.println(sources.get(i));
                    pw.close();
                }
                executeCUDA = new ExecuteCUDA(threadsPerBlock, blocksPerGrid, compileOptions, pathNvcc, parametrosByGPU.get(i), arquivo, metodo, i);
                threads[i] = new Thread(executeCUDA);
                threads[i].start();
            }

            for (int i = 0; i < threads.length; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(JSeriesCUDA.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            for (int i = 0; i < parametros.size(); i++) {
                parametro = parametros.get(i);
                if (parametro.isWrite()) {
                    if (parametro.isDivide()) {
                        long size = parametro.getSize();
                        int step = (int) size / sum;

                        int begin = 0;
                        int end;

                        for (int j = 0; j < proporcao.length; j++) {
                            if (j < proporcao.length - 1) {
                                end = begin + step * proporcao[j];
                            } else {
                                end = (int) (size);
                            }
                            parametro.copyFrom(parametrosByGPU.get(j).get(i), begin, end);
                            begin = end;
                        }
                    }
                }
            }
        } else {
            if (hasSizeof) {
                for (int i = 0; i < parametros.size(); i++) {
                    source = source.replace("#SIZEOF" + i + "#", "" + parametros.get(i).getSize());
                }
                PrintWriter pw = new PrintWriter(arquivo);
                pw.println(source);
                pw.close();
            }

            ExecuteCUDA executeCUDA = new ExecuteCUDA(threadsPerBlock, blocksPerGrid, compileOptions, pathNvcc, parametros, arquivo, metodo, 0);
            executeCUDA.run();
        }
        if (measure) {
            allTimes.setEndLong(System.nanoTime());
            allTimes.setEnd(new Date());
            allTimes.sum();
        }
    }

    public static void exec(List<ParameterGPU> parametros, String arquivo, String metodo) throws IOException {
        JSeriesCUDA cuda = new JSeriesCUDA();
        cuda.execute(parametros, arquivo, metodo);
    }

    /**
     * Fully reads the given InputStream and returns it as a byte array
     *
     * @param inputStream The input stream to read
     * @return The byte array containing the data from the input stream
     * @throws IOException If an I/O error occurs
     */
    private static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[8192];
        while (true) {
            int read = inputStream.read(buffer);
            if (read == -1) {
                break;
            }
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }

    private static int ceiling(int valor, int teto) {
        if ((valor % teto) > 0) {
            return ((valor / teto) + 1) * teto;
        } else {
            return valor;
        }
    }

    private static int floor(float valor, int divisor) {
        int div = (int) (valor / divisor);
        return divisor * div;
    }

    class ExecuteCUDA implements Runnable {

        private int warpSize;
        private int[] threadsPerBlock;
        private int[] blocksPerGrid;
        private String compileOptions;
        private String pathNvcc;
        private int registers;
        private int sharedMemory;
        private int usedSharedMemory = 0;
        private List<ParameterGPU> parametros;
        private String arquivo;
        private String metodo;
        private int indexThread;

        public ExecuteCUDA(int[] threadsPerBlock, int[] blocksPerGrid, String compileOptions, String pathNvcc, List<ParameterGPU> parametros, String arquivo, String metodo, int indexThread) {
            this.threadsPerBlock = threadsPerBlock;
            this.blocksPerGrid = blocksPerGrid;
            this.compileOptions = compileOptions;
            this.pathNvcc = pathNvcc;
            this.parametros = parametros;
            this.arquivo = arquivo;
            this.metodo = metodo;
            this.indexThread = indexThread;
        }

        public List<Occupancy> getMaxThreadsPerBlock(CUdevice device, int registerPerThread, int sharedMemoryPerBlock) {

            int threadPerBlock;
            List<Occupancy> occupancies = new ArrayList<Occupancy>();

            ComputeCapability compute;

            if (compileOptions != null && compileOptions.contains("-arch=sm_")) {
                String[] vet = compileOptions.split("-arch=sm_", -2);
                String comp = vet[1].replaceAll("[ ]+", "");
                vet = comp.split("|");
//            System.out.println(comp);
//            System.out.println(vet);
//            System.out.println(compileOptions);
//            System.out.println(vet[0] + "." + vet[1]);
                String number1 = null;
                String number2 = null;
                for (int i = 0; i < vet.length; i++) {
                    String string = vet[i];
                    if (number1 == null && string.matches("[0-3]")) {
                        number1 = string;
                    } else if (number2 == null && string.matches("[0-5]")) {
                        number2 = string;
                    }
                }
//            System.out.println(number1 + "." + number2);
                compute = gpuData.get(number1 + "." + number2);
            } else {
                // Obtain the compute capability
                int majorArray[] = {0};
                int minorArray[] = {0};
                cuDeviceComputeCapability(majorArray, minorArray, device);
                int major = majorArray[0];
                int minor = minorArray[0];

                compute = gpuData.get(major + "." + minor);
            }

            if (compute != null) {

                String topList = "21,22,29,30,37,38,45,46,";
                if (compute.getComputeCapability().contains("2.")) {
                    if (topList.contains(registers + ",")) {
                        compute.setRegisterAllocationUnitSize(128);
                    } else {
                        compute.setRegisterAllocationUnitSize(64);
                    }
                }

                int warpsPerMultiprocessor = compute.getWarpsMultiprocessor();
                int maxThreadsPerBlock = compute.getMaxThreadBlockSize();
                int sharedMemoryPerMultiprocessor = compute.getMaxSharedMemoryMultiprocessorBytes();
                int totalRegistersPerMultiprocessor = compute.getRegisterFileSize();
                warpSize = compute.getThreadsWarp();

                int sharedMemoryAllocationUnitSize = compute.getSharedMemoryAllocationUnitSize();
                int threadBlocksPerMultiprocessor = compute.getThreadBlocksMultiprocessor();
                String granularity = compute.getAllocationGranularity();
                int registerAllocationUnitSize = compute.getRegisterAllocationUnitSize();
                int warpAllocationGranularity = compute.getWarpAllocationGranularity();
                int registersPerThread = compute.getMaxRegistersThread();

                float bigIndexOccupancy = 0;
                int choosenThread = 0;

                warpSize = compute.getThreadsWarp();

                sharedMemoryPerMultiprocessor = compute.getMaxSharedMemoryMultiprocessorBytes();

                totalRegistersPerMultiprocessor = compute.getRegisterFileSize();

                warpsPerMultiprocessor = compute.getThreadsMultiprocessor() / warpSize;

//            System.out.println("warpsPerMultiprocessor:" + warpsPerMultiprocessor);
                maxThreadsPerBlock = compute.getMaxThreadBlockSize();

                for (int i = warpSize; i <= maxThreadsPerBlock; i = i + warpSize) {
                    threadPerBlock = i;
                    int sharedMemoryPerBlockAllocated = ceiling(sharedMemoryPerBlock, sharedMemoryAllocationUnitSize);

                    int sharedMemoryPerBlocksPerSmAllocated = 0;
                    if (sharedMemoryPerBlockAllocated > 0) {
                        sharedMemoryPerBlocksPerSmAllocated = sharedMemoryPerMultiprocessor / sharedMemoryPerBlockAllocated;
                    } else {
                        sharedMemoryPerBlocksPerSmAllocated = threadBlocksPerMultiprocessor;
                    }

                    int warpsPerBlock = ceiling(threadPerBlock / warpSize, 1);

                    int registersLimitPerSM = 0;
                    int registersLimitPerBlock = 0;
                    if (granularity.equals("block")) {
                        registersLimitPerSM = totalRegistersPerMultiprocessor;
                        registersLimitPerBlock = ceiling(ceiling(warpsPerBlock, warpAllocationGranularity) * registerPerThread * warpSize, registerAllocationUnitSize);
                    } else {
                        int aux = ceiling(registerPerThread * warpSize, registerAllocationUnitSize);
//                    if (threadPerBlock == 704) {
//                        System.out.println("aux:" + aux);
//                        System.out.println("registerAllocationUnitSize:" + registerAllocationUnitSize);
//                    }
                        registersLimitPerSM = floor(totalRegistersPerMultiprocessor / aux, warpAllocationGranularity);
                        registersLimitPerBlock = warpsPerBlock;
                    }

                    int warpsBlocksPerSM = 0;
                    int registerBlocksPerSM = 0;

                    warpsBlocksPerSM = Math.min(threadBlocksPerMultiprocessor, floor(warpsPerMultiprocessor / warpsPerBlock, 1));

                    if (registerPerThread > registersPerThread) {
                        registerBlocksPerSM = 0;
                    } else {
                        if (registerPerThread > 0) {
                            registerBlocksPerSM = floor(registersLimitPerSM / registersLimitPerBlock, 1);
                        } else {
                            registerBlocksPerSM = threadBlocksPerMultiprocessor;
                        }
                    }

                    int activeThreadBlocksPerMultiprocessor = 0;

                    activeThreadBlocksPerMultiprocessor = Math.min(warpsBlocksPerSM, registerBlocksPerSM);
                    activeThreadBlocksPerMultiprocessor = Math.min(activeThreadBlocksPerMultiprocessor, sharedMemoryPerBlocksPerSmAllocated);

                    int activeWarpsPerMultiprocessor = activeThreadBlocksPerMultiprocessor * warpsPerBlock;

                    float indexOccupancy = (float) (warpsPerMultiprocessor);
                    indexOccupancy = (float) (activeWarpsPerMultiprocessor / indexOccupancy);

                    if (bigIndexOccupancy <= indexOccupancy) {
                        bigIndexOccupancy = indexOccupancy;
                        choosenThread = threadPerBlock;
                    }

//                System.out.println(indexOccupancy + " " + threadPerBlock);
                    if (indexOccupancy > 0) {
//                    System.out.println("Add:");
                        occupancies.add(new Occupancy(indexOccupancy, threadPerBlock));
                    }

//                if (threadPerBlock == 704) {
//                    System.out.println("warpsBlocksPerSM:" + warpsBlocksPerSM);
//                    System.out.println("registerBlocksPerSM:" + registerBlocksPerSM);
//                    System.out.println("registersLimitPerSM:" + registersLimitPerSM);
//                    System.out.println("totalRegistersPerMultiprocessor:" + totalRegistersPerMultiprocessor);
//                }
                }

                if (print) {
                    System.out.println("Thread:" + choosenThread + " Occupancy:" + bigIndexOccupancy);
                }
//            System.out.println("Ocupado:" + bigIndexOccupancy);

                Collections.sort(occupancies);
                Collections.reverse(occupancies);
//            System.out.println("Ret:" + occupancies);
                return occupancies;
            }
            return null;
        }

        public int[] calculateThreadsPerBlock(int maxThreadsPerBlock, int dim, CUdevice device) {
            int[] blocks = new int[]{1, 1, 1};
            switch (dim) {
                case 3:
                    int y = 1;
                    int z = 1;

                    int factoration = maxThreadsPerBlock / warpSize;

                    int middle;
                    int div;
                    List<Integer> numbers = new ArrayList<Integer>();
                    while (factoration > 1) {
                        middle = factoration / 2;
                        div = 2;
                        while (factoration % div != 0 && div < middle) {
                            div++;
                        }
                        if (factoration % div == 0) {
                            numbers.add(div);
                            factoration = factoration / div;
                        } else {
                            numbers.add(factoration);
                            factoration = factoration / factoration;
                        }
                    }
                    numbers.add(1);
                    middle = numbers.size() / 2;
                    for (int i = 0; i < middle; i++) {
                        y = y * numbers.get(i);
                    }
                    for (int i = middle; i < numbers.size(); i++) {
                        z = z * numbers.get(i);
                    }
                    if (z > y) {
                        int a = z;
                        z = y;
                        y = a;
                    }

                    blocks[0] = warpSize;
                    blocks[1] = y;
                    blocks[2] = z;

                    break;
                case 2:

                    blocks[0] = warpSize;
                    blocks[1] = maxThreadsPerBlock / warpSize;
                    break;
                case 1:
                    blocks[0] = maxThreadsPerBlock;
                    break;
            }
            return blocks;
        }

        public int[] calculateBlocksPerGrid(int[] threadsPerBlock, Long[] sizes, int dim, CUdevice device) {
            int[] grids = new int[]{1, 1, 1};
            int array[] = {0};
            if (dim >= 1) {
                for (int i = 0; i < dim; i++) {
                    if (i == 0) {
                        cuDeviceGetAttribute(array, CU_DEVICE_ATTRIBUTE_MAX_GRID_DIM_X, device);
                        grids[0] = array[0];
                    } else if (i == 1) {
                        cuDeviceGetAttribute(array, CU_DEVICE_ATTRIBUTE_MAX_GRID_DIM_Y, device);
                        grids[1] = array[0];
                    } else if (i == 2) {
                        cuDeviceGetAttribute(array, CU_DEVICE_ATTRIBUTE_MAX_GRID_DIM_Z, device);
                        grids[2] = array[0];
                    }
                    if (threadsPerBlock[i] < sizes[i]) {
                        long total = (long) threadsPerBlock[i] * (long) grids[i];
//                        System.out.println("blocks:" + threadsPerBlock[i] + " " + sizes[i] + " " + grids[i] + " " + total + " = " + (total > sizes[i]));
                        if (total > sizes[i]) {
                            int mod = ((int) (sizes[i] % (threadsPerBlock[i])));
                            grids[i] = (int) (sizes[i] / threadsPerBlock[i]) + (mod == 0 ? 0 : 1);
//                            System.out.println("Dentro:" + grids[i]);
                        } else {
                            grids[i] = -1;
                        }
                    } else {
                        threadsPerBlock[i] = sizes[i].intValue();
                        grids[i] = 1;
                    }
                }
            } else {
                grids[0] = 1;
                threadsPerBlock[0] = 1;
            }
            return grids;
        }

        @Override
        public void run() {
            try {
                if (measure) {
                    time.setEndLong(System.nanoTime());
                    time.setEnd(new Date());
                    time.sum();

                    time = measures.get(EnumMeasure.CONFIG_TIME);
                    if (time == null) {
                        time = new MeasureTimeGPU();
                        time.setDescription("Tempo de execução das configurações");
                        measures.put(EnumMeasure.CONFIG_TIME, time);
                    } else {
                        time.sum();
                    }
                    time.setBeginLong(System.nanoTime());
                    time.setBegin(new Date());
                }

                JCudaDriver.setExceptionsEnabled(ExceptionsEnabled);

                CUdevice device = (CUdevice) devices.get(indexThread).getDevice();

//                int array2[] = {0};
//                cuDeviceGetAttribute(array2, CU_DEVICE_ATTRIBUTE_PCI_BUS_ID, device);
//                System.out.println(array2[0]);
                CUcontext context = new CUcontext();
                cuCtxCreate(context, 0, device);

                //        registers = 24;
                //        sharedMemory = 200;
                // Create the PTX file by calling the NVCC
                String ptxFileName = preparePtxFile(arquivo, device);

                //        System.exit(1);
                // Load the ptx file.
                CUmodule module = new CUmodule();
                cuModuleLoad(module, ptxFileName);

                // Obtain a function pointer to the "add" function.
                CUfunction function = new CUfunction();
                cuModuleGetFunction(function, module, metodo);

                // Allocate the device input data, and copy the
                // host input data to the device
                CUdeviceptr deviceInput;
                List<CUdeviceptr> ptrs = new ArrayList<CUdeviceptr>();
                List<Pointer> pointers = new ArrayList<Pointer>();

                if (measure) {
                    time.setEndLong(System.nanoTime());
                    time.setEnd(new Date());
                    time.sum();

                    time = measures.get(EnumMeasure.ALLOCATE_TIME);
                    if (time == null) {
                        time = new MeasureTimeGPU();
                        time.setDescription("Tempo de execução das alocações e envios de parâmetros");
                        measures.put(EnumMeasure.ALLOCATE_TIME, time);
                    } else {
                        time.sum();
                    }
                    time.setBeginLong(System.nanoTime());
                    time.setBegin(new Date());
                }

                Pointer aux = null;
                List<Long> tam = new ArrayList<Long>();
                int dim = 0;
                ParameterGPU parametro;
                for (int i = 0; i < parametros.size(); i++) {
                    parametro = parametros.get(i);
                    deviceInput = new CUdeviceptr();
                    ptrs.add(deviceInput);

                    if (parametro.getDataDouble() != null) {
                        aux = Pointer.to(parametro.getDataDouble());
                        cuMemAlloc(deviceInput, parametro.getSize() * Sizeof.DOUBLE);
                    } else if (parametro.getDataFloat() != null) {
                        aux = Pointer.to(parametro.getDataFloat());
                        cuMemAlloc(deviceInput, parametro.getSize() * Sizeof.FLOAT);
                    } else if (parametro.getDataInt() != null) {
                        aux = Pointer.to(parametro.getDataInt());
                        cuMemAlloc(deviceInput, parametro.getSize() * Sizeof.INT);
                    } else if (parametro.getDataLong() != null) {
                        aux = Pointer.to(parametro.getDataLong());
                        cuMemAlloc(deviceInput, parametro.getSize() * Sizeof.LONG);
                    } else if (parametro.getDataChar() != null) {
                        aux = Pointer.to(parametro.getDataChar());
                        cuMemAlloc(deviceInput, parametro.getSize() * Sizeof.CHAR);
                    } else if (parametro.getDataShort() != null) {
                        aux = Pointer.to(parametro.getDataShort());
                        cuMemAlloc(deviceInput, parametro.getSize() * Sizeof.SHORT);
                    }

                    if (parametro.isDefineThreads()) {
                        tam.add(parametro.getSize());
                        dim++;
                    }

                    pointers.add(aux);

                    if (parametro.isRead()) {
                        if (parametro.getDataDouble() != null) {
                            cuMemcpyHtoD(deviceInput, aux, parametro.getSize() * Sizeof.DOUBLE);
                        } else if (parametro.getDataFloat() != null) {
                            cuMemcpyHtoD(deviceInput, aux, parametro.getSize() * Sizeof.FLOAT);
                        } else if (parametro.getDataInt() != null) {
                            cuMemcpyHtoD(deviceInput, aux, parametro.getSize() * Sizeof.INT);
                        } else if (parametro.getDataLong() != null) {
                            cuMemcpyHtoD(deviceInput, aux, parametro.getSize() * Sizeof.LONG);
                        } else if (parametro.getDataChar() != null) {
                            cuMemcpyHtoD(deviceInput, aux, parametro.getSize() * Sizeof.CHAR);
                        } else if (parametro.getDataShort() != null) {
                            cuMemcpyHtoD(deviceInput, aux, parametro.getSize() * Sizeof.SHORT);
                        }
                    }

                }

                Pointer[] point = new Pointer[ptrs.size()];
                for (int i = 0; i < point.length; i++) {
                    point[i] = Pointer.to(ptrs.get(i));

                }
                // Set up the kernel parameters: A pointer to an array
                // of pointers which point to the actual values.
                Pointer kernelParameters = Pointer.to(point);
                if (measure) {
                    time.setEndLong(System.nanoTime());
                    time.setEnd(new Date());
                    time.sum();

                    time = measures.get(EnumMeasure.CALCULATE_THREADS_TIME);
                    if (time == null) {
                        time = new MeasureTimeGPU();
                        time.setDescription("Tempo de execução dos cálculos de Threads");
                        measures.put(EnumMeasure.CALCULATE_THREADS_TIME, time);
                    } else {
                        time.sum();
                    }
                    time.setBeginLong(System.nanoTime());
                    time.setBegin(new Date());
                }

                int array[] = {0};
                int[] blocks = new int[]{1, 1, 1};

                int[] grids = new int[]{1, 1, 1};
                int maxThreadsPerBlock = 192;

                if (!isManual()) {

                    List<Integer> ordem = new ArrayList<Integer>();
                    if (dim > 1) {
                        for (int i = 0; i < tam.size(); i++) {
                            ordem.add(-1);
                        }

                        double maior = 0;
                        int indiceMaior = 0;
                        for (int i = 0; i < ordem.size(); i++) {
                            maior = 0;
                            indiceMaior = 0;
                            for (int j = 0; j < tam.size(); j++) {
                                if (tam.get(j) > maior && !ordem.contains(j)) {
                                    maior = tam.get(j);
                                    indiceMaior = j;
                                }
                            }
                            ordem.set(i, indiceMaior);
                        }

                    }

                    List<Occupancy> occupancies = getMaxThreadsPerBlock(device, registers, sharedMemory);
                    //            System.out.println("occupancies:" + occupancies);
                    if (occupancies != null) {
                        boolean right = false;
                        while (!right && occupancies.size() > 0) {
                            if (occupancies.size() > 0) {
                                right = false;
                                Occupancy occ = occupancies.remove(0);
                                maxThreadsPerBlock = occ.getThreadsPerBlock();

                                if (print) {
                                    System.out.println("Occupancy:" + occ.getRatioOccupancy() + " MaxThreadsPerBlock: " + maxThreadsPerBlock + " Reg: " + registers + " Shared Memory: " + sharedMemory);
                                }

                                int[] b = calculateThreadsPerBlock(maxThreadsPerBlock, dim, device);
                                //                        System.out.println(Arrays.toString(b));

                                if (ordem.isEmpty() || ordem.size() == 2) {
                                    blocks = b;
                                } else {
                                    blocks[ordem.get(0)] = b[0];
                                    blocks[ordem.get(1)] = b[1];
                                    blocks[ordem.get(2)] = b[2];
                                }

                                //            blocks[0] = 769;
                                //            blocks[1] = 1;
                                //            blocks[2] = 1;
                                //            System.out.println("BlockX: " + blocks[0] + " BlockY: " + blocks[1] + " BlockZ: " + blocks[2]);
                                Long[] sizes = new Long[dim];
                                for (int i = 0; i < tam.size(); i++) {
                                    sizes[i] = tam.get(i);
                                }
                                int multThreads = 1;
                                grids = calculateBlocksPerGrid(blocks, sizes, dim, device);
                                for (int i = 0; i < grids.length; i++) {
                                    multThreads *= blocks[i];
                                    if (grids[i] == -1) {
                                        right = true;
                                        break;
                                    }
                                }
                                if (!right && multThreads != maxThreadsPerBlock) {
                                    switch (dim) {
                                        case 2:
                                            int lower = (blocks[0] > blocks[1] ? 1 : 0);
                                            blocks[(lower == 0 ? 1 : 0)] = maxThreadsPerBlock / blocks[lower];
                                            grids = calculateBlocksPerGrid(blocks, sizes, dim, device);
                                            for (int i = 0; i < grids.length; i++) {
                                                if (grids[i] == -1) {
                                                    right = true;
                                                    break;
                                                }
                                            }
                                            break;
                                        case 3:
                                            break;
                                    }
                                }

                                right = !right;
                            } else {
                                throw new DataSizeException();
                            }
                        }
                    }
                    if (occupancies == null || occupancies.isEmpty()) {
                        try {
                            throw new IOException("GPU doesn't support this data size!");
                        } catch (IOException ex) {
                            Logger.getLogger(JSeriesCUDA.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else {
                    blocks = threadsPerBlock;
                    grids = blocksPerGrid;
                }

                //        threadsPerBlock = 1024;
                //        grids[0] = 46;
                //        grids[1] = 1;
                //
                //        blocks[0] = 32;
                //        blocks[1] = 32;
                //        blocks[2] = 1;
                // Call the kernel function.
                if (print) {
                    System.out.println("Dim:" + dim);
                    System.out.println("GridX: " + grids[0] + " GridY: " + grids[1] + " GridZ: " + grids[2]);
                    System.out.println("Tam: " + tam + " Threads:" + (grids[0] * blocks[0]));
                    System.out.println("BlockX: " + blocks[0] + " BlockY: " + blocks[1] + " BlockZ: " + blocks[2]);
                    System.out.println("ThreadsPerBlock: " + maxThreadsPerBlock);
                    System.out.println("Used Shared Memory:" + usedSharedMemory);
                }

                if (measure) {
                    time.setEndLong(System.nanoTime());
                    time.setEnd(new Date());
                    time.sum();

                    time = measures.get(EnumMeasure.EXECUTION_TIME);
                    if (time == null) {
                        time = new MeasureTimeGPU();
                        time.setDescription("Tempo de execução na placa");
                        measures.put(EnumMeasure.EXECUTION_TIME, time);
                    } else {
                        time.sum();
                    }
                    time.setBeginLong(System.nanoTime());
                    time.setBegin(new Date());
                }
                // Call the kernel function.
                cuLaunchKernel(function,
                        grids[0], grids[1], grids[2], // Grid dimension
                        blocks[0], blocks[1], blocks[2], // Block dimension
                        usedSharedMemory, null, // Shared memory size and stream
                        kernelParameters, null // Kernel- and extra parameters
                );

                cuCtxSynchronize();

                if (measure) {
                    time.setEndLong(System.nanoTime());
                    time.setEnd(new Date());
                    time.sum();

                    time = measures.get(EnumMeasure.OUTPUT_TIME);
                    if (time == null) {
                        time = new MeasureTimeGPU();
                        time.setDescription("Tempo de execução de recebimento das saídas");
                        measures.put(EnumMeasure.OUTPUT_TIME, time);
                    } else {
                        time.sum();
                    }
                    time.setBeginLong(System.nanoTime());
                    time.setBegin(new Date());
                }

                // Allocate host output memory and copy the device output
                // to the host.
                for (int i = 0; i < parametros.size(); i++) {
                    parametro = parametros.get(i);
                    if (parametro.isWrite()) {
                        if (parametro.getDataDouble() != null) {
                            cuMemcpyDtoH(pointers.get(i), ptrs.get(i), parametro.getSize() * Sizeof.DOUBLE);
                        } else if (parametro.getDataFloat() != null) {
                            cuMemcpyDtoH(pointers.get(i), ptrs.get(i), parametro.getSize() * Sizeof.FLOAT);
                        } else if (parametro.getDataInt() != null) {
                            cuMemcpyDtoH(pointers.get(i), ptrs.get(i), parametro.getSize() * Sizeof.INT);
                        } else if (parametro.getDataLong() != null) {
                            cuMemcpyDtoH(pointers.get(i), ptrs.get(i), parametro.getSize() * Sizeof.LONG);
                        } else if (parametro.getDataChar() != null) {
                            cuMemcpyDtoH(pointers.get(i), ptrs.get(i), parametro.getSize() * Sizeof.CHAR);
                        } else if (parametro.getDataShort() != null) {
                            cuMemcpyDtoH(pointers.get(i), ptrs.get(i), parametro.getSize() * Sizeof.SHORT);
                        }
                    }
                }

                if (measure) {
                    time.setEndLong(System.nanoTime());
                    time.setEnd(new Date());
                    time.sum();

                    time = measures.get(EnumMeasure.FREE_TIME);
                    if (time == null) {
                        time = new MeasureTimeGPU();
                        time.setDescription("Tempo de execução para liberar as memórias");
                        measures.put(EnumMeasure.FREE_TIME, time);
                    } else {
                        time.sum();
                    }
                    time.setBeginLong(System.nanoTime());
                    time.setBegin(new Date());
                }
                // Clean up.
                for (int i = 0; i < parametros.size(); i++) {
                    cuMemFree(ptrs.get(i));
                    jcuda.runtime.JCuda.cudaFree(ptrs.get(i));
                    //            cuMemFreeHost(ptrs.get(i));
                }
                cuModuleUnload(module);

                cuCtxDestroy(context);

                jcuda.runtime.JCuda.cudaDeviceReset();

                if (measure) {
                    time.setEndLong(System.nanoTime());
                    time.setEnd(new Date());
                    time.sum();
                }
            } catch (IOException ex) {
                Logger.getLogger(JSeriesCUDA.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        private String preparePtxFile(String cuFileName, CUdevice device) throws IOException {
            int endIndex = cuFileName.lastIndexOf('.');
            if (endIndex == -1) {
                endIndex = cuFileName.length() - 1;
            }
            String ptxFileName = cuFileName.substring(0, endIndex) + indexThread + ".ptx";

            File cuFile = new File(cuFileName);
            if (!cuFile.exists()) {
                throw new IOException("Input file not found: " + cuFileName);
            }

            if (!(compileOptions != null && compileOptions.contains("-arch=sm_"))) {
                // Obtain the compute capability
                int majorArray[] = {0};
                int minorArray[] = {0};
                cuDeviceComputeCapability(majorArray, minorArray, device);
                int major = majorArray[0];
                int minor = minorArray[0];

                compileOptions = compileOptions + " -arch=sm_" + major + minor;
            }

            String modelString = "-m" + System.getProperty("sun.arch.data.model");
            String command = pathNvcc + "nvcc " + modelString + compileOptions + " -ptx " + cuFile.getPath() + " -o " + ptxFileName;

            if (print) {
                System.out.println("Command:" + command);
            }

//        System.out.println(command);
//        ptxas -v
//        System.out.println(command);
            Process process = Runtime.getRuntime().exec(command);

            String errorMessage = new String(toByteArray(process.getErrorStream()));
            String outputMessage = new String(toByteArray(process.getInputStream()));
            int exitValue = 0;
            try {
                exitValue = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for nvcc output", e);
            }

            if (exitValue != 0) {
                System.out.println("nvcc process exitValue " + exitValue);
                System.out.println("errorMessage:\n" + errorMessage);
                System.out.println("outputMessage:\n" + outputMessage);
                throw new IOException("Could not create .ptx file: " + errorMessage);
            }

            command = pathNvcc + "ptxas -v " + modelString + compileOptions.replace("-use_fast_math", "") + " " + ptxFileName;
//        System.out.println(command);
            process = Runtime.getRuntime().exec(command);
            outputMessage = new String(toByteArray(process.getInputStream()));

            errorMessage = new String(toByteArray(process.getErrorStream()));
            try {
                exitValue = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for nvcc output", e);
            }

            if (exitValue != 0) {
                System.out.println("nvcc process exitValue " + exitValue);
                System.out.println("errorMessage:\n" + errorMessage);
                System.out.println("outputMessage:\n" + outputMessage);
                throw new IOException("Could not create .ptx file: " + errorMessage);
            } else {
                String[] vet = errorMessage.split("\n", -2);

                String line = null;

                for (int i = 0; i < vet.length; i++) {
                    String string = vet[i];
                    if (string.contains("Used") && string.contains("registers")) {
                        line = string;
                        break;
                    }

                }

                if (print) {
                    System.out.println("Line:" + line);
                }
                if (line != null && line.matches(".+[Used ][0-9]+[ registers].+")) {
//                System.out.println("Line:" + line);
                    vet = line.split(":", -2);
                    vet = vet[1].split(",", -2);
                    String reg = vet[0].replaceAll("[ ]+", "");
                    reg = reg.replace("Used", "");
                    reg = reg.replace("registers", "");
                    registers = Integer.parseInt(reg);

                    reg = vet[1].replaceAll("[ ]+", "");
                    if (reg.contains("+")) {
                        sharedMemory = Integer.parseInt(reg.split("[+]", -2)[0]);
                    } else {
                        vet = vet[1].split("[ ]+", -2);
                        sharedMemory = Integer.parseInt(vet[1]);
                    }
                } else {
                    vet = outputMessage.split("\n", -2);
                    for (int i = 0; i < vet.length; i++) {
                        String string = vet[i];
                        if (string.matches(".+[Used ][0-9]+[ registers].+")) {
                            line = string;
                            break;
                        }

                    }
                    if (line != null && line.matches(".+[Used ][0-9]+[ registers].+")) {
                        vet = line.split(":", -2);
                        vet = vet[1].split(",", -2);

                        String reg = vet[0].replaceAll("[ ]+", "");
                        reg = reg.replace("Used", "");
                        reg = reg.replace("registers", "");
                        registers = Integer.parseInt(reg);

                        reg = vet[1].replaceAll("[ ]+", "");
                        if (reg.contains("+")) {
                            sharedMemory = Integer.parseInt(reg.split("[+]", -2)[0]);
                        } else {
                            vet = vet[1].split("[ ]+", -2);
                            sharedMemory = Integer.parseInt(vet[1]);
                        }
                    }
                }

            }

//        System.out.println("Finished creating PTX file");
            return ptxFileName;
        }
    }

    public int[] getBlocksPerGrid() {
        return blocksPerGrid;
    }

    public void setBlocksPerGrid(int[] blocksPergrid) {
        this.blocksPerGrid = blocksPergrid;
    }

    public int[] getThreadsPerBlock() {
        return threadsPerBlock;
    }

    public void setThreadsPerBlock(int[] threadsPerblock) {
        this.threadsPerBlock = threadsPerblock;
    }

    public String getCompileOptions() {
        return compileOptions;
    }

    public void setCompileOptions(String compileOptions) {
        this.compileOptions = compileOptions;
    }

    public String getPathNvcc() {
        return pathNvcc;
    }

    public void setPathNvcc(String pathNvcc) {
        this.pathNvcc = pathNvcc;
    }

    public int getUsedSharedMemory() {
        return usedSharedMemory;
    }

    public void setUsedSharedMemory(int usedSharedMemory) {
        this.usedSharedMemory = usedSharedMemory;
    }
}
