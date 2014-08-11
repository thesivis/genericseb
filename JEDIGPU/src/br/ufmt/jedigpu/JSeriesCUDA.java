/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

import br.ufmt.jedigpu.GPU;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import static jcuda.driver.JCudaDriver.*;
import static jcuda.driver.CUdevice_attribute.*;

import jcuda.*;
import jcuda.driver.*;

/**
 *
 * @author raphael
 */
public class JSeriesCUDA extends GPU {

    private int Threads = 20;
    private int warpSize;
    private int[] threadsPerBlock = new int[]{1, 1, 1};
    private int[] blocksPerGrid = new int[]{1, 1, 1};
    private String compileOptions = " -use_fast_math ";
    private String pathNvcc = "/usr/local/cuda/bin/";
    private int registers;
    private int sharedMemory;
    private int usedSharedMemory = 0;
    private CUdevice device;
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
    }

    private CUdevice getDevice() {
        if (device == null) {
            JCudaDriver.setExceptionsEnabled(ExceptionsEnabled);

//        teste(ptxFileName);
//        // Initialize the driver and create a context for the first device.
            cuInit(0);
            device = new CUdevice();
            cuDeviceGet(device, 0);
        }

        return device;
    }

    public void execute(List<ParameterGPU> parametros, String arquivo, String metodo) throws IOException {
        if (measure) {
            measures = new ArrayList<MeasureTimeGPU>();
            allTimes = new MeasureTimeGPU();
            allTimes.setBeginLong(System.nanoTime());
            allTimes.setBegin(new Date());
            allTimes.setDescription("Tempo de execução total");
            measures.add(allTimes);

            time = new MeasureTimeGPU();
            time.setBeginLong(System.nanoTime());
            time.setDescription("Tempo de execução das configurações");
            measures.add(time);
            time.setBegin(new Date());
        }

        CUdevice device = getDevice();

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

            time = new MeasureTimeGPU();
            time.setBeginLong(System.nanoTime());
            time.setDescription("Tempo de execução das alocações e envios de parâmetros");
            measures.add(time);
            time.setBegin(new Date());

        }

        Pointer aux = null;
        ParameterGPU parametro = null;
        List<Long> tam = new ArrayList<Long>();
        int dim = 0;
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

        if (measure) {
            time.setEnd(new Date());
            time.setEndLong(System.nanoTime());

            time = new MeasureTimeGPU();
            time.setBeginLong(System.nanoTime());
            time.setDescription("Tempo de execução dos cálculos de Threads");
            measures.add(time);
            time.setBegin(new Date());
        }

        Pointer[] point = new Pointer[ptrs.size()];
        for (int i = 0; i < point.length; i++) {
            point[i] = Pointer.to(ptrs.get(i));

        }
        // Set up the kernel parameters: A pointer to an array
        // of pointers which point to the actual values.
        Pointer kernelParameters = Pointer.to(point);

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

                        int[] b = calculateThreadsPerBlock(maxThreadsPerBlock, dim);
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
                        grids = calculateBlocksPerGrid(blocks, sizes, dim);
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
                                    grids = calculateBlocksPerGrid(blocks, sizes, dim);
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
                throw new IOException("GPU doesn't support this data size!");
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

            time = new MeasureTimeGPU();
            time.setBeginLong(System.nanoTime());
            time.setDescription("Tempo de execução na placa");
            measures.add(time);
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

            time = new MeasureTimeGPU();
            time.setBeginLong(System.nanoTime());
            time.setDescription("Tempo de execução de recebimento das saídas");
            measures.add(time);
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

            time = new MeasureTimeGPU();
            time.setBeginLong(System.nanoTime());
            time.setDescription("Tempo de execução para liberar as memórias");
            measures.add(time);
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

            allTimes.setEndLong(System.nanoTime());
            allTimes.setEnd(new Date());
        }

    }

    public static void exec(List<ParameterGPU> parametros, String arquivo, String metodo) throws IOException {
        JSeriesCUDA cuda = new JSeriesCUDA();
        cuda.execute(parametros, arquivo, metodo);
    }

    private String preparePtxFile(String cuFileName, CUdevice device) throws IOException {
        int endIndex = cuFileName.lastIndexOf('.');
        if (endIndex == -1) {
            endIndex = cuFileName.length() - 1;
        }
        String ptxFileName = cuFileName.substring(0, endIndex + 1) + "ptx";

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

    public int getMaxThreadPerBlock(String cuFileName) throws IOException {
        getDevice();
        int registers = 0;
        int sharedMemory = 0;
        String compileOptions = this.compileOptions;
        int endIndex = cuFileName.lastIndexOf('.');
        if (endIndex == -1) {
            endIndex = cuFileName.length() - 1;
        }
        String ptxFileName = cuFileName.substring(0, endIndex + 1) + "ptx";

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

        List<Occupancy> occupancies = getMaxThreadsPerBlock(device, registers, sharedMemory);

//        System.out.println("Finished creating PTX file");
        return occupancies.get(0).getThreadsPerBlock();
    }

    public int[] calculateThreadsPerBlock(int maxThreadsPerBlock, int dim) {
        getDevice();
        int[] blocks = new int[]{1, 1, 1};
        switch (dim) {
            case 3:
                int y = 1;
                int z = 1;

                int factoration = maxThreadsPerBlock / warpSize;

                int middle;
                int div;
                List<Integer> numbers = new ArrayList<>();
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

    public int[] calculateBlocksPerGrid(int[] threadsPerBlock, Long[] sizes, int dim) {
        getDevice();
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

    public void setManual(String code, Long[] sizes) throws IOException {
        int max = getMaxThreadPerBlock(code);
        int[] threadsPerBlock = calculateThreadsPerBlock(max, sizes.length);
        int[] blocksPerGrid = calculateBlocksPerGrid(threadsPerBlock, sizes, sizes.length);
        setManual(true);
        setThreadsPerBlock(threadsPerBlock);
        setBlocksPerGrid(blocksPerGrid);
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

    public int getThreads() {
        return Threads;
    }

    public void setThreads(int Threads) {
        this.Threads = Threads;
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

    public List<Occupancy> getMaxThreadsPerBlock(CUdevice device, int registerPerThread, int sharedMemoryPerBlock) {

        int threadPerBlock;
        List<Occupancy> occupancies = new ArrayList<>();

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
                } else if (number2 == null && string.matches("[0-3]")) {
                    number2 = string;
                }
            }
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

    public int getUsedSharedMemory() {
        return usedSharedMemory;
    }

    public void setUsedSharedMemory(int usedSharedMemory) {
        this.usedSharedMemory = usedSharedMemory;
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
}
