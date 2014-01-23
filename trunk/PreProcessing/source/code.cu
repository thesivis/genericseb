#include "Constants.h"

extern "C"{

    #define K1 607.760009765625f
    #define UR 36.459999084472656f
    #define K2 1260.56005859375f
    #define Kt 1.0f
    #define reflectanciaAtmosfera 0.029999999329447746f
    #define StefanBoltzman 5.669999936230852E-8f
    #define L 0.10000000149011612f
    #define julianDay 248.0f
    #define Uref 0.9207136034965515f
    #define Ta 32.7400016784668f
    #define P 99.30000305175781f
    #define Rg_24h 243.9499969482422f
    #define S 1367.0f
    #define latitude -16.559999465942383f
    #define Z 50.2400016784668f

    #define dr 0.985846566048888f
    #define cosZ 0.7687302359405433f
    #define declinacaoSolar 0.10110116629018565f
    #define anguloHorarioNascerSol 1.5406261742945568f
    #define rad_solar_toa 33.677489830352386f
    #define Rg_24h_mj 21.077279736328126f
    #define transmissividade24h 0.6258566134977164f
    #define ea 1.80713581697293f
    #define W 27.22280289964939f
    #define transmissividade 0.7299130836698269f
    #define emissivityAtm 0.7887452566722131f
    #define SWd 736.5564499520258f
    #define LWdAtm 391.54430119162527f

    __constant__ double parameterAlbedo[] = {0.2930000126361847f,0.27399998903274536f,0.2329999953508377f,0.15700000524520874f,0.032999999821186066f,0.0f,0.010999999940395355f};

    __constant__ double calibration1[] = {-1.5199999809265137,193.0,1957.0};
    __constant__ double calibration2[] = {-2.8399999141693115,365.0,1826.0};
    __constant__ double calibration3[] = {-1.1699999570846558,264.0,1554.0};
    __constant__ double calibration4[] = {-1.5099999904632568,221.0,1036.0};
    __constant__ double calibration5[] = {-0.3700000047683716,30.200000762939453,215.0};
    __constant__ double calibration6[] = {1.2378000020980835,15.303000450134277,1.0};
    __constant__ double calibration7[] = {-0.15000000596046448,16.5,80.66999816894531};

    __device__ void execute_sub(
        double pixel2,
        double pixel3,
        double pixel4,
        double pixel5,
        double pixel1,
        double pixel7,
        double pixel6,

        double * albedo,
        double * NDVI,
        double * SAVI,
        double * IAF,
        double * emissividadeNB,
        double * emissivity,
        double * Ts,
        double * LWd,
        double * Rn
    ){
        double sumBandas = 0;
        double banda1=calibration1[0]+((calibration1[1]-calibration1[0])/255.0f)*pixel1;
        double banda2=calibration2[0]+((calibration2[1]-calibration2[0])/255.0f)*pixel2;
        double banda3=calibration3[0]+((calibration3[1]-calibration3[0])/255.0f)*pixel3;
        double banda4=calibration4[0]+((calibration4[1]-calibration4[0])/255.0f)*pixel4;
        double banda5=calibration5[0]+((calibration5[1]-calibration5[0])/255.0f)*pixel5;
        double banda6=calibration6[0]+((calibration6[1]-calibration6[0])/255.0f)*pixel6;
        double banda7=calibration7[0]+((calibration7[1]-calibration7[0])/255.0f)*pixel7;
        double bandaRefletida1=(pi*banda1)/(calibration1[2]*cosZ*dr);
        sumBandas += parameterAlbedo[0]*bandaRefletida1;
        double bandaRefletida2=(pi*banda2)/(calibration2[2]*cosZ*dr);
        sumBandas += parameterAlbedo[1]*bandaRefletida2;
        double bandaRefletida3=(pi*banda3)/(calibration3[2]*cosZ*dr);
        sumBandas += parameterAlbedo[2]*bandaRefletida3;
        double bandaRefletida4=(pi*banda4)/(calibration4[2]*cosZ*dr);
        sumBandas += parameterAlbedo[3]*bandaRefletida4;
        double bandaRefletida5=(pi*banda5)/(calibration5[2]*cosZ*dr);
        sumBandas += parameterAlbedo[4]*bandaRefletida5;
        double bandaRefletida6=(pi*banda6)/(calibration6[2]*cosZ*dr);
        sumBandas += parameterAlbedo[5]*bandaRefletida6;
        double bandaRefletida7=(pi*banda7)/(calibration7[2]*cosZ*dr);
        sumBandas += parameterAlbedo[6]*bandaRefletida7;
        *albedo = (sumBandas-reflectanciaAtmosfera)/(transmissividade*transmissividade);
        *NDVI = (bandaRefletida4-bandaRefletida3)/(bandaRefletida4+bandaRefletida3);
        *SAVI = ((1.0f+L)*(bandaRefletida4-bandaRefletida3))/(L+bandaRefletida4+bandaRefletida3);
        *IAF = (-logf((0.69f- *SAVI)/0.59f)/0.91f);
        *emissividadeNB = 0.97f+0.0033f* *IAF;
        *emissivity = 0.95f+0.01f* *IAF;
        *Ts = K2/logf((( *emissividadeNB*K1)/banda6)+1.0f);
        *LWd =  *emissivity*StefanBoltzman*(powf( *Ts,4.0f));
        *Rn = ((1.0f- *albedo)*SWd)+( *emissivity*(LWdAtm)- *LWd);
    }

    __global__ void execute(
        double * pixel2,
        double * pixel3,
        double * pixel4,
        double * pixel5,
        double * pixel1,
        double * pixel7,
        double * pixel6,

        double * albedo,
        double * NDVI,
        double * SAVI,
        double * IAF,
        double * emissividadeNB,
        double * emissivity,
        double * Ts,
        double * LWd,
        double * Rn,
        int size){
        int idx = blockIdx.x*blockDim.x + threadIdx.x;
        if(idx < size){
            execute_sub(
                pixel2[idx],
                pixel3[idx],
                pixel4[idx],
                pixel5[idx],
                pixel1[idx],
                pixel7[idx],
                pixel6[idx],
                (albedo+idx),
                (NDVI+idx),
                (SAVI+idx),
                (IAF+idx),
                (emissividadeNB+idx),
                (emissivity+idx),
                (Ts+idx),
                (LWd+idx),
                (Rn+idx)
            );
        }
    }
}

