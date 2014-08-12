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

    public JSeriesCL() {
    }

    public JSeriesCL(OpenCLEnum deviceType) {
        this.deviceType = deviceType.getType();
    }

    public void execute(List<ParameterGPU> parametros, String codigoFonte, String metodo) {
        if (measure) {
            measures = new ArrayList<MeasureTimeGPU>();
            allTimes = new MeasureTimeGPU();
            allTimes.setBegin(new Date());
            allTimes.setDescription("Tempo de execução total");
            measures.add(allTimes);

            time = new MeasureTimeGPU();
            time.setDescription("Tempo de execução das configurações");
            measures.add(time);
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
            time.setEnd(new Date());

            time = new MeasureTimeGPU();
            time.setDescription("Tempo de execução das alocações e envios de parâmetros");
            measures.add(time);
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
            time.setEnd(new Date());

            time = new MeasureTimeGPU();
            time.setDescription("Tempo de execução dos cálculos de Threads");
            measures.add(time);
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
            time.setEnd(new Date());

            time = new MeasureTimeGPU();
            time.setDescription("Tempo de execução na placa");
            measures.add(time);
            time.setBegin(new Date());
        }
//        System.exit(1);
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, dim, null, global_work_size, local_work_size, 0, null, null);

        clFinish(commandQueue);
//        System.out.println("executado");

        if (measure) {
            time.setEnd(new Date());

            time = new MeasureTimeGPU();
            time.setDescription("Tempo de execução de recebimento das saídas");
            measures.add(time);
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
            time.setEnd(new Date());

            time = new MeasureTimeGPU();
            time.setDescription("Tempo de execução para liberar as memórias");
            measures.add(time);
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
            time.setEnd(new Date());

            allTimes.setEnd(new Date());
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
}
