/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.preprocessing.utils;

/**
 *
 * @author raphael
 */
public class Constants {

    public static String ARQUIVO = "";
    public static double pi = 3.1415926536;
    public static double deg2rad = 0.01745329252f;
    public static float kel2deg = -273.15f;  /* Kelvin -> Celsius */

    public static float k = 0.41f;  /* von Karman constant */

    public static double Sigma_SB = 5.678E-8f;  /* Stefan-Boltzmann's constant (W/m2/K4) */

    public static float T0 = 273.15f;
    public static float Rso = 1366.0f;  /* Solar Constant(W/m2) */

    public static double g = 9.81f;  /* Gravity accelaration (kg s-2) */

    public static double Rmax = 6378137.0f;  /* the earth's equatorial radius (m) */

    public static double Rmin = 6356752.0f;  /* the earth's polar radius (m) */

    public static double Rd = 287.04f;  /* Gas Constant for Dry air, from table 2.1 P25 of Brutsaert 2005 (J kg-1 K-1) */

    public static double Rv = 461.5f;  /* Gas Constant for Water vapor, from table 2.1 P25 of Brutsaert 2005 (J kg-1 K-1) */

    public static double Cpw = 1846.0f;  /* specific heat coefficient for water vapor, J Kg-1 K-1 */

    public static double Cpd = 1005.0f;  /* specific heat coefficient for dry air, J Kg-1 K-1 */

    public static double Cd = 0.2f;  /* Foliage drag coefficient */

    public static double Ct = 0.01f;  /* Heat transfer coefficient */

    public static double gammaConst = 67.0f;  /* psychometric constant (Pa K-1) */

    public static double Pr = 0.7f;  /* Prandtl Prandtl number */

    public static double Pr_u = 1.0f;  /* Turbulent Prandtl number for unstable case */

    public static double Pr_s = 0.95f;  /* Turbulent Prandtl number for stable case */

    public static double ri_i = 60.0f;  /* surface resistance of standard crop, s m-1 */

    /* The latent heat of vaporization at 30C from Brutsaert, 1982, p.41,tab. 3.4,
    more exact values can be obtained from eqn(3.22, 3.24a,b) */
    public static double L_e = 2.430f;   /* MJ Kg-1 */

    public static double rho_w = 0.998f;  /* density of water [kg/(m2 mm)] */

    public static double PSI0 = 1.3656120718024247f;
    public static double DT = 86400.0f;
    public static float MaxAllowedError = 0.01f;

    /* MODIS Tile Sizes */
    public static int dimx = 120;
    public static int dimy = 120;
    public static int SelectedDevice = 0;
    public static int nThreadsPerBlock = 64;
    
    public static float z200 = 200.0f;
    public static float z2 = 2.0f;
    public static float z1 = 0.1f;
    public static float p = 1.15f;
    public static float cp = 1004.0f;

}
