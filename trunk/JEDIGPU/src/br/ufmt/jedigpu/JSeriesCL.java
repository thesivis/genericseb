/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

import br.ufmt.jedigpu.GPU;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jocl.CL;
import static org.jocl.CL.*;

import org.jocl.CLException;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;

/**
 *
 * @author raphael
 */
public class JSeriesCL extends GPU {

    private long[] workGroups = new long[]{1};
    private long[] workItems = new long[]{1};
    private long deviceType = CL_DEVICE_TYPE_GPU;
    private List<Device> devices;

    public JSeriesCL() {
        setDevices();
    }

    public JSeriesCL(OpenCLEnum deviceType) {
        this.deviceType = deviceType.getType();
        setDevices();
    }

    public void execute(List<ParameterGPU> parametros, String codigoFonte, String metodo) {


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

        int minCores = devices.get(0).getCores();

        List<List<ParameterGPU>> parametrosByGPU = new ArrayList<List<ParameterGPU>>();

        if (devices.size() == 1) {
            parametrosByGPU.add(parametros);
        } else {
            parametrosByGPU.add(new ArrayList<ParameterGPU>());
        }
        for (int i = 1; i < devices.size(); i++) {
            if (devices.get(i).getCores() < minCores) {
                minCores = devices.get(i).getCores();
            }
            parametrosByGPU.add(new ArrayList<ParameterGPU>());
        }
        int[] proporcao = new int[devices.size()];
        int sum = 0;
        for (int i = 0; i < devices.size(); i++) {
            proporcao[i] = (int) Math.round(devices.get(i).getCores() / (float) minCores);
            sum += proporcao[i];
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
            }

        }

        Thread[] threads = new Thread[devices.size()];
        for (int i = 0; i < devices.size(); i++) {
            System.out.println("Thread:" + i);
            ExecuteOpenCL executeOpenCL = new ExecuteOpenCL(parametros, codigoFonte, metodo, workGroups, workItems);
            threads[i] = new Thread(executeOpenCL);
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(JSeriesCL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (int i = 0; i < parametros.size(); i++) {
            parametro = parametros.get(i);
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

        if (measure) {
            allTimes.setEndLong(System.nanoTime());
            allTimes.setEnd(new Date());
            allTimes.sum();
        }

    }

    public static void exec(List<ParameterGPU> parametros, String codigoFonte, String metodo) {
        JSeriesCL opencl = new JSeriesCL();
        opencl.execute(parametros, codigoFonte, metodo);
    }

    public long[] getWorkGroups() {
        return workGroups;
    }

    public void setWorkGroups(long[] workGroups) {
        this.workGroups = workGroups;
    }

    public long[] getWorkItems() {
        return workItems;
    }

    public void setWorkItems(long[] workItems) {
        this.workItems = workItems;
    }

    private static int getInt(cl_device_id device, int paramName) {
        return getInts(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    private static int[] getInts(cl_device_id device, int paramName, int numValues) {
        int values[] = new int[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_int * numValues, Pointer.to(values), null);
        return values;
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static String getString(cl_device_id device, int paramName) {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int) size[0]];
        clGetDeviceInfo(device, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length - 1);
    }

    private void setDevices() {

        // Obtain the number of platforms
        int numPlatforms[] = new int[1];
        clGetPlatformIDs(0, null, numPlatforms);

        // Obtain the platform IDs
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms[0]];
        clGetPlatformIDs(platforms.length, platforms, null);

        // Collect all devices of all platforms
        List<cl_device_id> devicesCL = new ArrayList<cl_device_id>();
        for (int i = 0; i < platforms.length; i++) {
            try {
                // Obtain the number of devices for the current platform
                int numDevices[] = new int[1];
                clGetDeviceIDs(platforms[i], deviceType, 0, null, numDevices);

                cl_device_id devicesArray[] = new cl_device_id[numDevices[0]];
                clGetDeviceIDs(platforms[i], deviceType, numDevices[0], devicesArray, null);

                devicesCL.addAll(Arrays.asList(devicesArray));
            } catch (CLException ex) {
            }
        }

        devices = new ArrayList<Device>();
        // Print the infos about all devices
        for (cl_device_id deviceID : devicesCL) {
            // CL_DEVICE_NAME
            String deviceName = getString(deviceID, CL_DEVICE_NAME);
            // CL_DEVICE_MAX_COMPUTE_UNITS
            int maxComputeUnits = getInt(deviceID, CL_DEVICE_MAX_COMPUTE_UNITS);
            devices.add(new Device(deviceName, maxComputeUnits));
        }
    }

    class ExecuteOpenCL implements Runnable {

        private List<ParameterGPU> parametros;
        private String codigoFonte;
        private String metodo;
        private long[] workGroups;
        private long[] workItems;

        public ExecuteOpenCL(List<ParameterGPU> parametros, String codigoFonte, String metodo, long[] workGroups, long[] workItems) {
            this.parametros = parametros;
            this.codigoFonte = codigoFonte;
            this.metodo = metodo;
            this.workGroups = workGroups;
            this.workItems = workItems;
        }

        @Override
        public void run() {
            if (measure) {
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

            List<Pointer> pointers = new ArrayList<Pointer>(parametros.size());
            Pointer dst = null;
            for (ParameterGPU parametro : parametros) {
                if (parametro.getDataDouble() != null) {
                    dst = Pointer.to(parametro.getDataDouble());
                } else if (parametro.getDataFloat() != null) {
                    dst = Pointer.to(parametro.getDataFloat());
                } else if (parametro.getDataInt() != null) {
                    dst = Pointer.to(parametro.getDataInt());
                } else if (parametro.getDataLong() != null) {
                    dst = Pointer.to(parametro.getDataLong());
                } else if (parametro.getDataChar() != null) {
                    dst = Pointer.to(parametro.getDataChar());
                } else if (parametro.getDataShort() != null) {
                    dst = Pointer.to(parametro.getDataShort());
                }
                pointers.add(dst);
            }

            // The platform, device type and device number
            // that will be used
            final int platformIndex = 0;
//        final long deviceType = CL_DEVICE_TYPE_CPU;
            final int deviceIndex = 0;

            // Enable exceptions and subsequently omit error checks in this sample
            CL.setExceptionsEnabled(true);

            // Obtain the number of platforms
            int numPlatformsArray[] = new int[1];
            clGetPlatformIDs(0, null, numPlatformsArray);
            int numPlatforms = numPlatformsArray[0];
//        System.out.println("numPlatforms:" + numPlatforms);

            // Obtain a platform ID
            cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
            clGetPlatformIDs(platforms.length, platforms, null);
            cl_platform_id platform = platforms[platformIndex];

            cl_device_id device = null;
//        System.out.println(deviceType);

            for (int i = 0; i < platforms.length; i++) {
                platform = platforms[i];
                try {
//                System.out.println("Plat:"+platform);
                    // Obtain the number of devices for the platform
                    int numDevicesArray[] = new int[1];
                    clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
                    int numDevices = numDevicesArray[0];

                    // Obtain a device ID 
                    cl_device_id devices[] = new cl_device_id[numDevices];
                    clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
                    device = devices[deviceIndex];
                    break;
                } catch (CLException ex) {
                }
            }

//        System.out.println("platform:" + platform);
            // Initialize the context properties
            cl_context_properties contextProperties = new cl_context_properties();
            contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

            // Create a context for the selected device
            cl_context context = clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);

            // Create a command-queue for the selected device
            cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

            int[] error = null;

            // Create the program from the source code
            cl_program program = clCreateProgramWithSource(context, 1, new String[]{codigoFonte}, null, null);

            // Build the program
            clBuildProgram(program, 0, null, null, null, null);

            // Create the kernel
            cl_kernel kernel = clCreateKernel(program, metodo, error);

            // Allocate the memory objects for the input- and output data
            cl_mem memObjects[] = new cl_mem[parametros.size()];

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

            ParameterGPU parametro = null;
            for (int i = 0; i < parametros.size(); i++) {
                parametro = parametros.get(i);
                if (deviceType == CL_DEVICE_TYPE_CPU || (parametro.isRead() && parametro.isWrite())) {
                    if (parametro.getDataDouble() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double * parametro.getDataDouble().length, pointers.get(i), error);
                    } else if (parametro.getDataFloat() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * parametro.getDataFloat().length, pointers.get(i), error);
                    } else if (parametro.getDataInt() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * parametro.getDataInt().length, pointers.get(i), error);
                    } else if (parametro.getDataLong() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_long * parametro.getDataLong().length, pointers.get(i), error);
                    } else if (parametro.getDataChar() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_char * parametro.getDataChar().length, pointers.get(i), error);
                    } else if (parametro.getDataShort() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_short * parametro.getDataShort().length, pointers.get(i), error);
                    }
                } else if (parametro.isRead()) {
                    if (parametro.getDataDouble() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_ONLY, Sizeof.cl_double * parametro.getDataDouble().length, pointers.get(i), error);
                    } else if (parametro.getDataFloat() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_ONLY, Sizeof.cl_float * parametro.getDataFloat().length, pointers.get(i), error);
                    } else if (parametro.getDataInt() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_ONLY, Sizeof.cl_int * parametro.getDataInt().length, pointers.get(i), error);
                    } else if (parametro.getDataLong() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_ONLY, Sizeof.cl_long * parametro.getDataLong().length, pointers.get(i), error);
                    } else if (parametro.getDataChar() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_ONLY, Sizeof.cl_char * parametro.getDataChar().length, pointers.get(i), error);
                    } else if (parametro.getDataShort() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_READ_ONLY, Sizeof.cl_short * parametro.getDataShort().length, pointers.get(i), error);
                    }

                } else if (parametro.isWrite()) {
                    if (parametro.getDataDouble() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_double * parametro.getDataDouble().length, pointers.get(i), error);
                    } else if (parametro.getDataFloat() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_float * parametro.getDataFloat().length, pointers.get(i), error);
                    } else if (parametro.getDataInt() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_int * parametro.getDataInt().length, pointers.get(i), error);
                    } else if (parametro.getDataLong() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_long * parametro.getDataLong().length, pointers.get(i), error);
                    } else if (parametro.getDataChar() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_char * parametro.getDataChar().length, pointers.get(i), error);
                    } else if (parametro.getDataShort() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_short * parametro.getDataShort().length, pointers.get(i), error);
                    }
                } else {
                    if (parametro.getDataDouble() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double * parametro.getDataDouble().length, pointers.get(i), error);
                    } else if (parametro.getDataFloat() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * parametro.getDataFloat().length, pointers.get(i), error);
                    } else if (parametro.getDataInt() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * parametro.getDataInt().length, pointers.get(i), error);
                    } else if (parametro.getDataLong() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_long * parametro.getDataLong().length, pointers.get(i), error);
                    } else if (parametro.getDataChar() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_char * parametro.getDataChar().length, pointers.get(i), error);
                    } else if (parametro.getDataShort() != null) {
                        memObjects[i] = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_short * parametro.getDataShort().length, pointers.get(i), error);
                    }
                }
            }

//        long globalSize = 0;
            List<Long> globais = new ArrayList<Long>();
            int dim = 0;
            for (int i = 0; i < parametros.size(); i++) {
                parametro = parametros.get(i);
                if (parametro.isDefineThreads()) {
                    dim++;
                    globais.add((long) parametro.getSize());
                }
                if (parametro.isRead()) {
                    if (parametro.getDataDouble() != null) {
                        clEnqueueWriteBuffer(commandQueue, memObjects[i], CL_TRUE, 0, Sizeof.cl_double * parametro.getDataDouble().length, pointers.get(i), 0, null, null);
                    } else if (parametro.getDataFloat() != null) {
                        clEnqueueWriteBuffer(commandQueue, memObjects[i], CL_TRUE, 0, Sizeof.cl_float * parametro.getDataFloat().length, pointers.get(i), 0, null, null);
                    } else if (parametro.getDataInt() != null) {
                        clEnqueueWriteBuffer(commandQueue, memObjects[i], CL_TRUE, 0, Sizeof.cl_int * parametro.getDataInt().length, pointers.get(i), 0, null, null);
                    } else if (parametro.getDataLong() != null) {
                        clEnqueueWriteBuffer(commandQueue, memObjects[i], CL_TRUE, 0, Sizeof.cl_long * parametro.getDataLong().length, pointers.get(i), 0, null, null);
                    } else if (parametro.getDataChar() != null) {
                        clEnqueueWriteBuffer(commandQueue, memObjects[i], CL_TRUE, 0, Sizeof.cl_char * parametro.getDataChar().length, pointers.get(i), 0, null, null);
                    } else if (parametro.getDataShort() != null) {
                        clEnqueueWriteBuffer(commandQueue, memObjects[i], CL_TRUE, 0, Sizeof.cl_short * parametro.getDataShort().length, pointers.get(i), 0, null, null);
                    }
                }
            }

            // Set the arguments for the kernel
            for (int i = 0; i < parametros.size(); i++) {
                clSetKernelArg(kernel, i, Sizeof.cl_mem, Pointer.to(memObjects[i]));
            }

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

            // Set the work-item dimensions
            long global_work_size[] = new long[globais.size()];
            long local_work_size[] = new long[globais.size()];

            if (isManual()) {
                global_work_size = workItems;
                local_work_size = workGroups;
            } else {
                List<Integer> ordem = new ArrayList<Integer>();
                for (int i = 0; i < global_work_size.length; i++) {
                    global_work_size[i] = globais.get(i);
                    ordem.add(-1);
                }

                double maior = 0;
                int indiceMaior = 0;
                for (int i = 0; i < ordem.size(); i++) {
                    maior = 0;
                    indiceMaior = 0;
                    for (int j = 0; j < global_work_size.length; j++) {
                        if (global_work_size[j] > maior && !ordem.contains(j)) {
                            maior = global_work_size[j];
                            indiceMaior = j;
                        }
                    }
                    ordem.set(i, indiceMaior);
                }

                double[] proporcao = new double[globais.size() - 1];

                for (int i = 1; i < global_work_size.length; i++) {
                    proporcao[i - 1] = (((double) global_work_size[ordem.get(0)]) / ((double) global_work_size[ordem.get(i)]));
                }

                Pointer pointerLocal = Pointer.to(local_work_size);

                clGetKernelWorkGroupInfo(kernel, device, CL_KERNEL_WORK_GROUP_SIZE, Sizeof.cl_long, pointerLocal, null);
                long local_work_sizeTotal = local_work_size[0];
                if (print) {
                    System.out.println("Max Work-Group: " + local_work_sizeTotal);
                }

                if (dim == 1) {
                    local_work_size[0] = local_work_sizeTotal;
                } else if (dim == 2) {

                    double a = proporcao[0];
                    int x = (int) Math.sqrt(local_work_sizeTotal * a) + 1;
                    int y = (int) (x / a);
                    if (y == 0) {
                        y = 1;
                    }
                    int total = x * y;

                    while (x > 1 && total > local_work_sizeTotal) {
                        x = x - 1;
                        y = (int) (x / a);
                        if (y == 0) {
                            y = 1;
                        }
                        total = x * y;
                    }

                    while (x > 1 && ((x + 1) * y) <= local_work_sizeTotal) {
                        x = x + 1;
                    }

                    local_work_size[ordem.get(0)] = x;
                    local_work_size[ordem.get(1)] = y;

                } else if (dim == 3) {

                    double a = proporcao[0];
                    double b = proporcao[1];

                    int x = (int) Math.cbrt(local_work_sizeTotal * a * b) + 1;
                    int y = (int) (x / a);
                    int z = (int) (x / b);

                    if (y == 0) {
                        y = 1;
                    }
                    if (z == 0) {
                        z = 1;
                    }

                    int total = x * y * z;
                    while (x > 1 && total > local_work_sizeTotal) {
                        x = x - 1;
                        y = (int) (x / a);
                        z = (int) (x / b);

                        if (y == 0) {
                            y = 1;
                        }
                        if (z == 0) {
                            z = 1;
                        }

                        total = x * y * z;
                    }

                    while (x > 1 && ((x + 1) * y * z) <= local_work_sizeTotal) {
                        x = x + 1;
                    }

                    local_work_size[ordem.get(0)] = x;
                    local_work_size[ordem.get(1)] = y;
                    local_work_size[ordem.get(2)] = z;

                }
                for (int i = 0; i < local_work_size.length; i++) {
                    if (local_work_size[i] <= 1) {
                        local_work_size[i] = 1;
                    } else {
                        global_work_size[i] = local_work_size[i] * ((global_work_size[i] / local_work_size[i]) + 1);
                    }
                }
            }
//        for (int j = 0; j < local_work_size.length; j++) {
//            if (global_work_size[j] > local_work_sizeTotal) {
//                for (long i = local_work_size[j]; i >= 1; i--) {
//                    if (global_work_size[j] % i == 0) {
//                        local_work_size[j] = i;
//                        break;
//                    }
//                }
//            } else {
//                local_work_size[j] = global_work_size[j];
//            }
//        }
//        for (int j = 0; j < local_work_size.length; j++) {
//            System.out.println("Local: " + local_work_size[j]);
//        }

//        local_work_size[1] = local_work_size[0]/local_work_sizeTotal;
//        System.out.println("Dim: " + dim + " " + (local_work_size[0]*local_work_size[1]));
            if (print) {
                System.out.println("Global: " + Arrays.toString(global_work_size));
                System.out.println("Local: " + Arrays.toString(local_work_size));
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
//        System.exit(1);
            // Execute the kernel
            clEnqueueNDRangeKernel(commandQueue, kernel, dim, null, global_work_size, local_work_size, 0, null, null);

            clFinish(commandQueue);
//        System.out.println("executado");

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

            // Read the output data
            for (int i = 0; i < parametros.size(); i++) {
                parametro = parametros.get(i);
                if (parametro.isWrite()) {
                    if (parametro.getDataDouble() != null) {
                        clEnqueueReadBuffer(commandQueue, memObjects[i], CL_TRUE, 0, parametro.getDataDouble().length * Sizeof.cl_double, pointers.get(i), 0, null, null);
                    } else if (parametro.getDataFloat() != null) {
                        clEnqueueReadBuffer(commandQueue, memObjects[i], CL_TRUE, 0, parametro.getDataFloat().length * Sizeof.cl_float, pointers.get(i), 0, null, null);
                    } else if (parametro.getDataInt() != null) {
                        clEnqueueReadBuffer(commandQueue, memObjects[i], CL_TRUE, 0, parametro.getDataInt().length * Sizeof.cl_int, pointers.get(i), 0, null, null);
                    } else if (parametro.getDataLong() != null) {
                        clEnqueueReadBuffer(commandQueue, memObjects[i], CL_TRUE, 0, parametro.getDataLong().length * Sizeof.cl_long, pointers.get(i), 0, null, null);
                    } else if (parametro.getDataChar() != null) {
                        clEnqueueReadBuffer(commandQueue, memObjects[i], CL_TRUE, 0, parametro.getDataChar().length * Sizeof.cl_char, pointers.get(i), 0, null, null);
                    } else if (parametro.getDataShort() != null) {
                        clEnqueueReadBuffer(commandQueue, memObjects[i], CL_TRUE, 0, parametro.getDataShort().length * Sizeof.cl_short, pointers.get(i), 0, null, null);
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
            // Release kernel, program, and memory objects

            for (int i = 0; i < memObjects.length; i++) {
//            System.out.println("Release:"+i);
                clReleaseMemObject(memObjects[i]);
            }
//         System.out.println("executado2");
            clReleaseKernel(kernel);
            clReleaseProgram(program);
            clReleaseCommandQueue(commandQueue);
            clReleaseContext(context);

            if (measure) {
                time.setEndLong(System.nanoTime());
                time.setEnd(new Date());
                time.sum();
            }
        }
    }
}
