/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericsebalgpu.utils;



import br.ufmt.preprocessing.exceptions.EnterFileException;
import br.ufmt.preprocessing.utils.DataStructure;
import br.ufmt.preprocessing.utils.ParameterEnum;
import br.ufmt.preprocessing.utils.Utilities;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author raphael
 */
public class SebalImportFiles {

    public List<DataStructure> executeImport(List<File> entradas) {
        if (entradas != null && entradas.size() == 12) {
            /* Input Variables */
            float[] SWd, LWd;
            float[] Albedo, Emissivity;
            float[] NDVI, LST_K, Uref;
            float[] SAVI;
            float[] a, b, Rg_24h, Tao_24h;

//            System.out.println("Lendo os dados");
            /* Loading the input Variables *******************************************/
            if (entradas.get(0).getPath().endsWith(".dat")) {
                NDVI = Utilities.ReadTXT(entradas.get(0).getPath());
                LST_K = Utilities.ReadTXT(entradas.get(1).getPath());
                Uref = Utilities.ReadTXT(entradas.get(2).getPath());
                SAVI = Utilities.ReadTXT(entradas.get(3).getPath());
                a = Utilities.ReadTXT(entradas.get(4).getPath());
                b = Utilities.ReadTXT(entradas.get(5).getPath());
                Albedo = Utilities.ReadTXT(entradas.get(6).getPath());
                Emissivity = Utilities.ReadTXT(entradas.get(7).getPath());
                SWd = Utilities.ReadTXT(entradas.get(8).getPath());
                LWd = Utilities.ReadTXT(entradas.get(9).getPath());
                Rg_24h = Utilities.ReadTXT(entradas.get(10).getPath());
                Tao_24h = Utilities.ReadTXT(entradas.get(11).getPath());
            } else {
                NDVI = Utilities.ReadTIFF(entradas.get(0).getPath());
                LST_K = Utilities.ReadTIFF(entradas.get(1).getPath());
                Uref = Utilities.ReadTIFF(entradas.get(2).getPath());
                SAVI = Utilities.ReadTIFF(entradas.get(3).getPath());
                a = Utilities.ReadTXT(entradas.get(4).getPath());
                b = Utilities.ReadTXT(entradas.get(5).getPath());
                Albedo = Utilities.ReadTIFF(entradas.get(6).getPath());
                Emissivity = Utilities.ReadTIFF(entradas.get(7).getPath());
                SWd = Utilities.ReadTIFF(entradas.get(8).getPath());
                LWd = Utilities.ReadTIFF(entradas.get(9).getPath());
                Rg_24h = Utilities.ReadTIFF(entradas.get(10).getPath());
                Tao_24h = Utilities.ReadTIFF(entradas.get(11).getPath());
            }
            /* END OF Loading the input Variables ************************************/


            List<DataStructure> outputs = new ArrayList<DataStructure>();
            outputs.add(new DataStructure(ParameterEnum.NDVI, NDVI));
            outputs.add(new DataStructure(ParameterEnum.LST_K, LST_K));
            outputs.add(new DataStructure(ParameterEnum.Uref, Uref));
            outputs.add(new DataStructure(ParameterEnum.SAVI, SAVI));
            outputs.add(new DataStructure(ParameterEnum.A, a));
            outputs.add(new DataStructure(ParameterEnum.B, b));
            outputs.add(new DataStructure(ParameterEnum.Albedo, Albedo));
            outputs.add(new DataStructure(ParameterEnum.Emissivity, Emissivity));
            outputs.add(new DataStructure(ParameterEnum.SWnet, SWd));
            outputs.add(new DataStructure(ParameterEnum.LWnet, LWd));
            outputs.add(new DataStructure(ParameterEnum.Rg_24h, Rg_24h));
            outputs.add(new DataStructure(ParameterEnum.Tao_24h, Tao_24h));

            return outputs;
        } else {
            throw new EnterFileException();
        }
    }
}
