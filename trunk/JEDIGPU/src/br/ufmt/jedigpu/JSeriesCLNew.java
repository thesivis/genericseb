/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.jedigpu;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.util.Filter;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author raphael
 */
public class JSeriesCLNew extends GPU {

    private long[] workGroups = new long[]{1};
    private long[] workItems = new long[]{1};
    private long deviceType = OpenCLEnum.GPU.getType();

    public JSeriesCLNew() {
    }

    public JSeriesCLNew(OpenCLEnum deviceType) {
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

        CLPlatform.initialize();
        CLPlatform[] listPlatorm = CLPlatform.listCLPlatforms(new Filter<CLPlatform>() {

            @Override
            public boolean accept(CLPlatform i) {
                if (deviceType == OpenCLEnum.CPU.getType()) {
                    CLDevice[] vet = i.listCLDevices(CLDevice.Type.CPU);
                    if (vet != null && vet.length > 0) {
                        return true;
                    }
                } else if (deviceType == OpenCLEnum.GPU.getType()) {
                    CLDevice[] vet = i.listCLDevices(CLDevice.Type.GPU);
                    if (vet != null && vet.length > 0) {
                        return true;
                    }
                }
                return false;
            }
        });
        CLPlatform cLPlatform = listPlatorm[0];

        CLContext context = CLContext.create(cLPlatform);

        CLDevice device = context.getMaxFlopsDevice();
        System.out.println("Platform:"+cLPlatform);
        System.out.println("Using:" + device);
//        Set<String> r = device.getExtensions();
//        for (Iterator<String> it = r.iterator(); it.hasNext();) {
//            String string = it.next();
//            System.out.println("Ex:"+string);
//        }

//        System.out.println(codigoFonte);
        CLCommandQueue queue = device.createCommandQueue();
        System.out.println("Queue:"+queue);

        CLProgram program = context.createProgram(codigoFonte);

        program = program.build();

        CLKernel kernel = program.createCLKernel(metodo);
        // Allocate the memory objects for the input- and output data
        CLBuffer memObjects[] = new CLBuffer[parametros.size()];

        if (measure) {
            time.setEnd(new Date());

            time = new MeasureTimeGPU();
            time.setDescription("Tempo de execução das alocações e envios de parâmetros");
            measures.add(time);
            time.setBegin(new Date());
        }

        long memorySize = 0;
        ParameterGPU parametro = null;
        for (int i = 0; i < parametros.size(); i++) {
            parametro = parametros.get(i);
            if ((parametro.isRead() && parametro.isWrite())) {
                if (parametro.getDataDouble() != null) {
                    memObjects[i] = context.createDoubleBuffer(parametro.getDataDouble().length, READ_WRITE);
                } else if (parametro.getDataFloat() != null) {
                    memObjects[i] = context.createFloatBuffer(parametro.getDataFloat().length, READ_WRITE);
                } else if (parametro.getDataInt() != null) {
                    memObjects[i] = context.createIntBuffer(parametro.getDataInt().length, READ_WRITE);
                } else if (parametro.getDataLong() != null) {
                    memObjects[i] = context.createLongBuffer(parametro.getDataLong().length, READ_WRITE);
                } else if (parametro.getDataChar() != null) {
                    memObjects[i] = context.createByteBuffer(parametro.getDataChar().length, READ_WRITE);
                } else if (parametro.getDataShort() != null) {
                    memObjects[i] = context.createShortBuffer(parametro.getDataShort().length, READ_WRITE);
                }
            } else if (parametro.isRead()) {
                if (parametro.getDataDouble() != null) {
                    memObjects[i] = context.createDoubleBuffer(parametro.getDataDouble().length, READ_ONLY);
                } else if (parametro.getDataFloat() != null) {
                    memObjects[i] = context.createFloatBuffer(parametro.getDataFloat().length, READ_ONLY);
                } else if (parametro.getDataInt() != null) {
                    memObjects[i] = context.createIntBuffer(parametro.getDataInt().length, READ_ONLY);
                } else if (parametro.getDataLong() != null) {
                    memObjects[i] = context.createLongBuffer(parametro.getDataLong().length, READ_ONLY);
                } else if (parametro.getDataChar() != null) {
                    memObjects[i] = context.createByteBuffer(parametro.getDataChar().length, READ_ONLY);
                } else if (parametro.getDataShort() != null) {
//                    System.out.println("short");
                    memObjects[i] = context.createShortBuffer(parametro.getDataShort().length, READ_ONLY);
                }

            } else if (parametro.isWrite()) {
                if (parametro.getDataDouble() != null) {
                    memObjects[i] = context.createDoubleBuffer(parametro.getDataDouble().length, WRITE_ONLY);
                } else if (parametro.getDataFloat() != null) {
                    memObjects[i] = context.createFloatBuffer(parametro.getDataFloat().length, WRITE_ONLY);
                } else if (parametro.getDataInt() != null) {
                    memObjects[i] = context.createIntBuffer(parametro.getDataInt().length, WRITE_ONLY);
                } else if (parametro.getDataLong() != null) {
                    memObjects[i] = context.createLongBuffer(parametro.getDataLong().length, WRITE_ONLY);
                } else if (parametro.getDataChar() != null) {
                    memObjects[i] = context.createByteBuffer(parametro.getDataChar().length, WRITE_ONLY);
                } else if (parametro.getDataShort() != null) {
                    memObjects[i] = context.createShortBuffer(parametro.getDataShort().length, WRITE_ONLY);
                }
            } else {
                if (parametro.getDataDouble() != null) {
                    memObjects[i] = context.createDoubleBuffer(parametro.getDataDouble().length, READ_WRITE);
                } else if (parametro.getDataFloat() != null) {
                    memObjects[i] = context.createFloatBuffer(parametro.getDataFloat().length, READ_WRITE);
                } else if (parametro.getDataInt() != null) {
                    memObjects[i] = context.createIntBuffer(parametro.getDataInt().length, READ_WRITE);
                } else if (parametro.getDataLong() != null) {
                    memObjects[i] = context.createLongBuffer(parametro.getDataLong().length, READ_WRITE);
                } else if (parametro.getDataChar() != null) {
                    memObjects[i] = context.createByteBuffer(parametro.getDataChar().length, READ_WRITE);
                } else if (parametro.getDataShort() != null) {
                    memObjects[i] = context.createShortBuffer(parametro.getDataShort().length, READ_WRITE);
                }
            }
            memorySize += memObjects[i].getCLSize();
        }

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
                    DoubleBuffer cLBuffer = (DoubleBuffer) memObjects[i].getBuffer();
                    cLBuffer.put(parametro.getDataDouble());
                    cLBuffer.rewind();
                } else if (parametro.getDataFloat() != null) {
                    FloatBuffer cLBuffer = (FloatBuffer) memObjects[i].getBuffer();
                    cLBuffer.put(parametro.getDataFloat());
                    cLBuffer.rewind();
                } else if (parametro.getDataInt() != null) {
                    IntBuffer cLBuffer = (IntBuffer) memObjects[i].getBuffer();
                    cLBuffer.put(parametro.getDataInt());
                    cLBuffer.rewind();
                } else if (parametro.getDataLong() != null) {
                    LongBuffer cLBuffer = (LongBuffer) memObjects[i].getBuffer();
                    cLBuffer.put(parametro.getDataLong());
                    cLBuffer.rewind();
                } else if (parametro.getDataChar() != null) {
                    CharBuffer cLBuffer = (CharBuffer) memObjects[i].getBuffer();
                    cLBuffer.put(parametro.getDataChar());
                    cLBuffer.rewind();
                } else if (parametro.getDataShort() != null) {
                    ShortBuffer cLBuffer = (ShortBuffer) memObjects[i].getBuffer();
                    cLBuffer.put(parametro.getDataShort());
                    cLBuffer.rewind();
                }
                queue = queue.putWriteBuffer(memObjects[i], false);
            }
        }

        // Set the arguments for the kernel
        kernel.putArgs(memObjects);

        System.out.println("Ocupando:" + (memorySize / 1000000) + "MB");
//        System.out.println("executand");
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

//            clGetKernelWorkGroupInfo(kernel, device, CL_KERNEL_WORK_GROUP_SIZE, Sizeof.cl_long, pointerLocal, null);
            long local_work_sizeTotal = device.getMaxWorkGroupSize();
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
        queue = queue.put1DRangeKernel(kernel, 0, global_work_size[0], local_work_size[0]);
//        clEnqueueNDRangeKernel(commandQueue, kernel, dim, null, global_work_size, local_work_size, 0, null, null);

        queue = queue.finish();
//        System.out.println("executado");

        if (measure) {
            time.setEnd(new Date());

            time = new MeasureTimeGPU();
            time.setDescription("Tempo de execução de recebimento das saídas");
            measures.add(time);
            time.setBegin(new Date());
        }

//        System.out.println("lendo");
        // Read the output data
        boolean escreveu = false;
        for (int i = 0; i < parametros.size(); i++) {
            parametro = parametros.get(i);
            if (parametro.isWrite()) {
                queue = queue.putReadBuffer(memObjects[i], true);

                if (parametro.getDataDouble() != null) {
                    DoubleBuffer cLBuffer = (DoubleBuffer) memObjects[i].getBuffer();
                    double[] array = parametro.getDataDouble();
                    for (int j = 0; j < array.length; j++) {
                        array[j] = cLBuffer.get();
                    }
                } else if (parametro.getDataFloat() != null) {
                    FloatBuffer cLBuffer = (FloatBuffer) memObjects[i].getBuffer();
                    float[] array = parametro.getDataFloat();
                    for (int j = 0; j < array.length; j++) {
                        array[j] = cLBuffer.get();
                    }
                } else if (parametro.getDataInt() != null) {
                    IntBuffer cLBuffer = (IntBuffer) memObjects[i].getBuffer();
                    int[] array = parametro.getDataInt();
                    for (int j = 0; j < array.length; j++) {
                        array[j] = cLBuffer.get();
                    }
                } else if (parametro.getDataLong() != null) {
                    LongBuffer cLBuffer = (LongBuffer) memObjects[i].getBuffer();
                    long[] array = parametro.getDataLong();
                    for (int j = 0; j < array.length; j++) {
                        array[j] = cLBuffer.get();
                    }
                } else if (parametro.getDataChar() != null) {
                    CharBuffer cLBuffer = (CharBuffer) memObjects[i].getBuffer();
                    char[] array = parametro.getDataChar();
                    for (int j = 0; j < array.length; j++) {
                        array[j] = cLBuffer.get();
                    }
                } else if (parametro.getDataShort() != null) {
                    ShortBuffer cLBuffer = (ShortBuffer) memObjects[i].getBuffer();
                    short[] array = parametro.getDataShort();
                    for (int j = 0; j < array.length; j++) {
                        array[j] = cLBuffer.get();
                    }
                }

//
////                System.out.println("copiando");
                if (!escreveu) {
                    System.out.println("merda");
                    escreveu = true;
                    float[] a = parametro.getDataFloat();
                    for (int j = 0; j < a.length; j++) {
                        if (a[j] != -99999.0) {
                            System.out.println(j);
                            System.out.print(a[j] + ", ");
                            break;
                        }
                    }
                    System.out.println("");
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
//        System.out.println("liberando");
        for (int i = 0; i < memObjects.length; i++) {
            memObjects[i].release();
        }
//        System.out.println("liberando1");
        kernel.release();
//        System.out.println("liberando2");
        program.release();
//        System.out.println("liberando3");
        queue.release();
//        System.out.println("liberando4");
        context.release();
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
