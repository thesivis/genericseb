import static br.ufmt.genericseb.Constants.*;
import br.ufmt.genericseb.Constants;
import java.util.HashMap;
import java.util.Map;
import br.ufmt.genericseb.GenericSEB;
import br.ufmt.genericlexerseb.Maths;
import java.util.List;

public class Equation{
    public Map<String, float[]> execute(short[]pixel1,short[]pixel2,short[]pixel3,short[]pixel4,short[]pixel5,short[]pixel6,short[]pixel7){

        Map<String, float[]> ret = new HashMap<String, float[]>();

        float K1 = 607.76f;
        float Tao_24h = 0.59930485f;
        float UR = 68.59f;
        float K2 = 1260.56f;
        float Kt = 1.0f;
        float reflectanciaAtmosfera = 0.03f;
        float StefanBoltzman = 5.67E-8f;
        float L = 0.1f;
        float julianDay = 157.0f;
        float Uref = 2.24f;
        float Ta = 20.53f;
        float indexMin = 294.01495f;
        float SAVI_hot = -0.3548628f;
        float P = 99.3f;
        float S = 1367.0f;
        float Rg_24h = 181.61319f;
        float RnHot = 385.1896f;
        float GHot = 42.624226f;
        float latitude = -16.56f;
        float indexMax = 296.85895f;
        float Z = 39.8911f;

        float[] parameterAlbedo = new float[]{0.2934178f,0.27377668f,0.23299503f,0.15533003f,0.032235477f,0.0f,0.012095051f};

        float[][] calibration = new float[][]{{-1.52f,193.0f,1957.0f}
            ,{-2.84f,365.0f,1826.0f}
            ,{-1.17f,264.0f,1554.0f}
            ,{-1.51f,221.0f,1036.0f}
            ,{-0.37f,30.2f,215.0f}
            ,{1.2378f,15.303f,1.0f}
            ,{-0.15f,16.5f,80.67f}
            };

        float dr = (float)(1.0+0.033*Math.cos(julianDay*2.0*Math.PI/365.0));

        float cosZ = (float)(Math.cos(((90.0-Z)*Math.PI)/180.0));

        float declinacaoSolar = (float)(Math.toRadians(23.45*Math.sin(Math.toRadians(360.0*(julianDay-80.0)/365.0))));

        float anguloHorarioNascerSol = (float)(Math.acos(-Math.tan(Math.PI*latitude/180.0)*Math.tan(declinacaoSolar)));

        float rad_solar_toa = (float)(24.0*60.0*0.082*dr*(anguloHorarioNascerSol*Math.sin(Math.PI*latitude/180.0)*Math.sin(declinacaoSolar)+Math.cos(Math.PI*latitude/180.0)*Math.cos(declinacaoSolar)*Math.sin(anguloHorarioNascerSol))/Math.PI);

        float Rg_24h_mj = (float)(0.0864*Rg_24h);

        float transmissividade24h = (float)(Rg_24h_mj/rad_solar_toa);

        float ea = (float)((0.61078*Math.exp(17.269*Ta/(237.3+Ta)))*UR/100.0);

        float W = (float)(0.14*ea*P+2.1);

        float transmissividade = (float)(0.35+0.627*Math.exp((-0.00146*P/(Kt*cosZ))-0.075*Math.pow((W/cosZ),0.4)));

        float emissivityAtm = (float)(0.625*Math.pow((1000.0*ea/(Ta+T0)),0.131));

        float SWd = (float)((S*cosZ*cosZ)/(1.085*cosZ+10.0*ea*(2.7+cosZ)*0.0010+0.2));

        float LWdAtm = (float)(emissivityAtm*StefanBoltzman*(Math.pow(Ta+T0,4.0)));


        float sumBandas = 0.0f;
        float banda1 = 0.0f;
        float banda2 = 0.0f;
        float banda3 = 0.0f;
        float banda4 = 0.0f;
        float banda5 = 0.0f;
        float banda6 = 0.0f;
        float banda7 = 0.0f;
        float bandaRefletida1 = 0.0f;
        float bandaRefletida2 = 0.0f;
        float bandaRefletida3 = 0.0f;
        float bandaRefletida4 = 0.0f;
        float bandaRefletida5 = 0.0f;
        float bandaRefletida6 = 0.0f;
        float bandaRefletida7 = 0.0f;

        float z0mHot;
        float U_starHot;
        float r_ahHot;
        float LHot;
        float tm_200Hot;
        float th_2Hot;
        float th_0_1Hot;

        float LPixel;
        float tm_200Pixel;
        float th_2Pixel;
        float th_0_1Pixel;

        float HHot = RnHot - GHot;
        float a = 0.0f;
        float b = 0.0f;
        float errorH = 10.0f;
        int step = 1;
        float r_ah_anteriorHot;
        float z0m = 0;
        float U_star = 0;
        float H = 0;
        float r_ah = 0;
        float albedo = 0;
        float NDVI = 0;
        float SAVI = 0;
        float IAF = 0;
        float emissividadeNB = 0;
        float emissivity = 0;
        float Ts = 0;
        float LWd = 0;
        float Rn = 0;
        float G0 = 0;
        float[] LE = new float[pixel7.length];
        ret.put("LE",LE);

        float[] evap_fr = new float[pixel7.length];
        ret.put("evap_fr",evap_fr);

        float[] Rn_24h = new float[pixel7.length];
        ret.put("Rn_24h",Rn_24h);

        float[] LE_24h = new float[pixel7.length];
        ret.put("LE_24h",LE_24h);

        float[] ET_24h = new float[pixel7.length];
        ret.put("ET_24h",ET_24h);

        for(int i = 0;i < pixel7.length;i++){
            if(!(pixel1[i] == pixel2[i] && pixel1[i] == pixel3[i] && pixel1[i] == pixel4[i] && pixel1[i] == pixel5[i] && pixel1[i] == pixel6[i] && pixel1[i] == pixel7[i])){
                z0m = (float)(0.0);

                U_star = (float)(0.0);

                H = (float)(0.0);

                r_ah = (float)(0.0);

                banda1= (float)(calibration[0][0]+((calibration[0][1]-calibration[0][0])/255.0)*pixel1[i]);

                banda2= (float)(calibration[1][0]+((calibration[1][1]-calibration[1][0])/255.0)*pixel2[i]);

                banda3= (float)(calibration[2][0]+((calibration[2][1]-calibration[2][0])/255.0)*pixel3[i]);

                banda4= (float)(calibration[3][0]+((calibration[3][1]-calibration[3][0])/255.0)*pixel4[i]);

                banda5= (float)(calibration[4][0]+((calibration[4][1]-calibration[4][0])/255.0)*pixel5[i]);

                banda6= (float)(calibration[5][0]+((calibration[5][1]-calibration[5][0])/255.0)*pixel6[i]);

                banda7= (float)(calibration[6][0]+((calibration[6][1]-calibration[6][0])/255.0)*pixel7[i]);

                sumBandas = 0.0f;
                bandaRefletida1= (float)((Math.PI*banda1)/(calibration[0][2]*cosZ*dr));

                sumBandas += parameterAlbedo[0]*bandaRefletida1;
                bandaRefletida2= (float)((Math.PI*banda2)/(calibration[1][2]*cosZ*dr));

                sumBandas += parameterAlbedo[1]*bandaRefletida2;
                bandaRefletida3= (float)((Math.PI*banda3)/(calibration[2][2]*cosZ*dr));

                sumBandas += parameterAlbedo[2]*bandaRefletida3;
                bandaRefletida4= (float)((Math.PI*banda4)/(calibration[3][2]*cosZ*dr));

                sumBandas += parameterAlbedo[3]*bandaRefletida4;
                bandaRefletida5= (float)((Math.PI*banda5)/(calibration[4][2]*cosZ*dr));

                sumBandas += parameterAlbedo[4]*bandaRefletida5;
                bandaRefletida6= (float)((Math.PI*banda6)/(calibration[5][2]*cosZ*dr));

                sumBandas += parameterAlbedo[5]*bandaRefletida6;
                bandaRefletida7= (float)((Math.PI*banda7)/(calibration[6][2]*cosZ*dr));

                sumBandas += parameterAlbedo[6]*bandaRefletida7;
                albedo = (float)((sumBandas-reflectanciaAtmosfera)/(transmissividade*transmissividade));

                NDVI = (float)((bandaRefletida4-bandaRefletida3)/(bandaRefletida4+bandaRefletida3));

                SAVI = (float)(((1.0+L)*(bandaRefletida4-bandaRefletida3))/(L+bandaRefletida4+bandaRefletida3));

                IAF = (float)((-Math.log((0.69-SAVI)/0.59)/0.91));

            if(SAVI <= 0.1f ){
                    IAF = (float)(0.0);

            }

            if(SAVI >= 0.687f ){
                    IAF = (float)(6.0);

            }

                emissividadeNB = (float)(0.97+0.0033*IAF);

            if(IAF >= 3.0f ){
                    emissividadeNB = (float)(0.98);

            }

            if(NDVI <= 0.0f ){
                    emissividadeNB = (float)(0.99);

            }

                emissivity = (float)(0.95+0.01*IAF);

            if(IAF >= 3.0f ){
                    emissivity = (float)(0.98);

            }

            if(NDVI <= 0.0f ){
                    emissivity = (float)(0.985);

            }

                Ts = (float)(K2/Math.log(((emissividadeNB*K1)/banda6)+1.0));

                z0m = (float) Math.exp(-5.809f + 5.62f * SAVI);
                U_star = (float) (k * Uref / Math.log(z200 / z0m));
                r_ah = (float) (Math.log(z2 / z1) / (U_star * k));

                H = 0f;
                LHot = 0f;
                tm_200Hot = 0f;
                th_2Hot = 0f;
                th_0_1Hot = 0f;

                LPixel = 0f;
                tm_200Pixel = 0f;
                th_2Pixel = 0f;
                th_0_1Pixel = 0f;

                HHot = RnHot - GHot;
                a = 0.0f;
                b = 0.0f;
                errorH = 10.0f;
                step = 1;
                z0mHot = (float) Math.exp(-5.809f + 5.62f * SAVI_hot);
                U_starHot = (float) (k * Uref / Math.log(z200 / z0mHot));
                r_ahHot = (float) (Math.log(z2 / z1) / (U_starHot * k));
                while (errorH > MaxAllowedError && step < 15) {

                    a = ((HHot) * r_ahHot) / (p * cp * (indexMax - indexMin));
                    b = -a * (indexMin - T0);

                    //PARTE DO PIXEL QUENTE
                    HHot = p * cp * (b + a * (indexMax - T0)) / r_ahHot;
                    LHot = (float) (-(p * cp * U_starHot * U_starHot * U_starHot * (indexMax)) / (k * g * HHot));

                    tm_200Hot = GenericSEB.Psim(LHot);
                    th_2Hot = GenericSEB.Psih(z2, LHot);
                    th_0_1Hot = GenericSEB.Psih(z1, LHot);

                    U_starHot = (float) (k * Uref / (Math.log(z200 / z0mHot) - tm_200Hot));
                    r_ah_anteriorHot = r_ahHot;
                    r_ahHot = (float) ((Math.log(z2 / z1) - th_2Hot + th_0_1Hot) / (U_starHot * k));

                    //PARTE DE CADA PIXEL
                    H = p * cp * (b + a * (Ts - T0)) / r_ah;
                    LPixel = (float) (-(p * cp * U_star * U_star * U_star * (Ts)) / (k * g * H));

                    tm_200Pixel = GenericSEB.Psim(LPixel);
                    th_2Pixel = GenericSEB.Psih(z2, LPixel);
                    th_0_1Pixel = GenericSEB.Psih(z1, LPixel);

                    U_star = (float) (k * Uref / (Math.log(z200 / z0m) - tm_200Pixel));
                    r_ah = (float) ((Math.log(z2 / z1) - th_2Pixel + th_0_1Pixel) / (U_star * k));

                    errorH = Math.abs(((r_ahHot - r_ah_anteriorHot) * 100) / r_ahHot);

                    step++;
                }

                LWd = (float)(emissivity*StefanBoltzman*(Math.pow(Ts,4.0)));

                Rn = (float)(((1.0-albedo)*SWd)+(emissivity*(LWdAtm)-LWd));

                G0 = (float)(Rn*(((Ts-T0)/albedo)*(0.0038*albedo+0.0074*albedo*albedo)*(1.0-0.98*Math.pow(NDVI,4.0))));

                LE[i] = (float)(Rn-H-G0);

                evap_fr[i] = (float)(LE[i]/(Rn-G0));

                Rn_24h[i] = (float)(Rg_24h*(1.0-albedo)-110.0*Tao_24h);

                LE_24h[i] = (float)(evap_fr[i]*Rn_24h[i]);

                ET_24h[i] = (float)((evap_fr[i]*Rn_24h[i]*86.4)/2450.0);

            }
        }
        return ret;
    }
}

