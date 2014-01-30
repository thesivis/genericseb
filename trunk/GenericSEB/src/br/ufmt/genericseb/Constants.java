/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericseb;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author raphael
 */
public class Constants {

    public static String ARQUIVO = "";
    public static float pi = 3.1415926536f;
    public static float deg2rad = 0.01745329252f;
    public static float kel2deg = -273.15f;  /* Kelvin -> Celsius */

    public static float k = 0.41f;  /* von Karman constant */

    public static float Sigma_SB = 5.678E-8f;  /* Stefan-Boltzmann's constant (W/m2/K4) */

    public static float T0 = 273.15f;
    public static float Rso = 1366.0f;  /* Solar Constant(W/m2) */

    public static float g = 9.81f;  /* Gravity accelaration (kg s-2) */

    public static float Rmax = 6378137.0f;  /* the earth's equatorial radius (m) */

    public static float Rmin = 6356752.0f;  /* the earth's polar radius (m) */

    public static float Rd = 287.04f;  /* Gas Constant for Dry air, from table 2.1 P25 of Brutsaert 2005 (J kg-1 K-1) */

    public static float Rv = 461.5f;  /* Gas Constant for Water vapor, from table 2.1 P25 of Brutsaert 2005 (J kg-1 K-1) */

    public static float Cpw = 1846.0f;  /* specific heat coefficient for water vapor, J Kg-1 K-1 */

    public static float Cpd = 1005.0f;  /* specific heat coefficient for dry air, J Kg-1 K-1 */

    public static float Cd = 0.2f;  /* Foliage drag coefficient */

    public static float Ct = 0.01f;  /* Heat transfer coefficient */

    public static float gammaConst = 67.0f;  /* psychometric constant (Pa K-1) */

    public static float Pr = 0.7f;  /* Prandtl Prandtl number */

    public static float Pr_u = 1.0f;  /* Turbulent Prandtl number for unstable case */

    public static float Pr_s = 0.95f;  /* Turbulent Prandtl number for stable case */

    public static float ri_i = 60.0f;  /* surface resistance of standard crop, s m-1 */

    /* The latent heat of vaporization at 30C from Brutsaert, 1982, p.41,tab. 3.4,
     more exact values can be obtained from eqn(3.22, 3.24a,b) */
    public static float L_e = 2.430f;   /* MJ Kg-1 */

    public static float rho_w = 0.998f;  /* density of water [kg/(m2 mm)] */

    public static float PSI0 = 1.3656120718024247f;
    public static float DT = 86400.0f;
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
    static Map<String, Float> variables = new HashMap<>();

    static {
        variables.put("pi", pi);
        variables.put("deg2rad", deg2rad);
        variables.put("kel2deg", (float) kel2deg);
        variables.put("k", (float) k);
        variables.put("Sigma_SB", Sigma_SB);
        variables.put("T0", (float) T0);
        variables.put("Rso", (float) Rso);
        variables.put("g", g);
        variables.put("Rmax", Rmax);
        variables.put("Rmin", Rmin);
        variables.put("Rd", Rd);
        variables.put("Rv", Rv);
        variables.put("Cpw", Cpw);
        variables.put("Cpd", Cpd);
        variables.put("Cd", Cd);
        variables.put("Ct", Ct);
        variables.put("gammaConst", gammaConst);
        variables.put("Pr", Pr);
        variables.put("Pr_u", Pr_u);
        variables.put("Pr_s", Pr_s);
        variables.put("ri_i", ri_i);
        variables.put("L_e", L_e);
        variables.put("rho_w", rho_w);
        variables.put("PSI0", PSI0);
        variables.put("DT", DT);
        variables.put("MaxAllowedError", (float) MaxAllowedError);
        variables.put("dimx", (float) dimx);
        variables.put("dimy", (float) dimy);
        variables.put("SelectedDevice", (float) SelectedDevice);
        variables.put("nThreadsPerBlock", (float) nThreadsPerBlock);
        variables.put("z200", (float) z200);
        variables.put("z2", (float) z2);
        variables.put("z1", (float) z1);
        variables.put("p", (float) p);
        variables.put("cp", (float) cp);
    }
}
