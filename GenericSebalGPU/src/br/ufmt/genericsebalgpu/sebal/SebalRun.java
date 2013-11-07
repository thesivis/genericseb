/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.sebal;

import br.ufmt.genericsebalgpu.utils.DataStructure;
import br.ufmt.genericsebalgpu.utils.ExecutionEnum;
import br.ufmt.genericsebalgpu.utils.ParameterEnum;
import br.ufmt.genericsebalgpu.utils.SebalImportFiles;
import br.ufmt.genericsebalgpu.utils.Utilities;

import java.io.File;
import java.util.List;

/**
 *
 * @author raphael
 */
public class SebalRun {

    private String option;

    public List<DataStructure> calculate(List<File> entradas, ExecutionEnum type) {

        SebalImportFiles impor = new SebalImportFiles();
        List<DataStructure> datas = impor.executeImport(entradas);

        Sebal sebal = new SebalImpl();

        if (type.equals(ExecutionEnum.CUDA)) {
            sebal = new SebalCUDA();
            if (option != null) {
                SebalCUDA cuda = (SebalCUDA) sebal;
                cuda.setOption(option);
            }
        } else if (type.equals(ExecutionEnum.OPENCL)) {
            sebal = new SebalOpenCL();
        }

        float a = Utilities.getData(datas, ParameterEnum.A)[0];
        float b = Utilities.getData(datas, ParameterEnum.B)[0];

        if (type.equals(ExecutionEnum.NORMAL)) {

            List<DataStructure> ret = null;
//            for (int i = 0; i < 20; i++) {
                long tempo = System.currentTimeMillis();
                ret = sebal.executar(a,b,
                        Utilities.getData(datas, ParameterEnum.SWd),
                        Utilities.getData(datas, ParameterEnum.LWd),
                        Utilities.getData(datas, ParameterEnum.Albedo),
                        Utilities.getData(datas, ParameterEnum.Emissivity),
                        Utilities.getData(datas, ParameterEnum.NDVI),
                        Utilities.getData(datas, ParameterEnum.LST_K),
                        Utilities.getData(datas, ParameterEnum.Uref),
                        Utilities.getData(datas, ParameterEnum.SAVI),
                        Utilities.getData(datas, ParameterEnum.Rg_24h),
                        Utilities.getData(datas, ParameterEnum.Tao_24h));
                tempo = System.currentTimeMillis() - tempo;
                System.out.println("Tempo:" + tempo);
//            }
            datas = ret;
        } else {
            datas = sebal.executar(a,b,
                    Utilities.getData(datas, ParameterEnum.SWd),
                    Utilities.getData(datas, ParameterEnum.LWd),
                    Utilities.getData(datas, ParameterEnum.Albedo),
                    Utilities.getData(datas, ParameterEnum.Emissivity),
                    Utilities.getData(datas, ParameterEnum.NDVI),
                    Utilities.getData(datas, ParameterEnum.LST_K),
                    Utilities.getData(datas, ParameterEnum.Uref),
                    Utilities.getData(datas, ParameterEnum.SAVI),
                    Utilities.getData(datas, ParameterEnum.Rg_24h),
                    Utilities.getData(datas, ParameterEnum.Tao_24h));
        }


//        System.out.println(Utilities.getData(datas, ParameterEnum.Rn)[653]);
//        System.out.println(Utilities.getData(datas, ParameterEnum.H)[653]);
//        System.out.println(Utilities.getData(datas, ParameterEnum.G0)[653]);
//        System.out.println(Utilities.getData(datas, ParameterEnum.LE)[653]);

        return datas;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
