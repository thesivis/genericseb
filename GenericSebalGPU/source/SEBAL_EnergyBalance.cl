
#ifdef cl_khr_fp64
	#pragma OPENCL EXTENSION cl_khr_fp64: enable
#else
	#pragma OPENCL EXTENSION cl_amd_fp64: enable
#endif

#include "./source/Constants.h"


void SEBAL_EnergyBalance_G(
			float SWd,
			float LWd,
			float albedo,
			float emissivity,
			float LST_K,
			float NDVI,
			float Uref,
			float SAVI,
			float a,
			float b,
			float Rg_24h,
			float Tao_24h,

			__global float * z0m,
			__global float * U_star,
			__global float * r_ah,
			__global float * Rn,
			__global float * G0,
			__global float * H,
			__global float * LE,
			__global float * evap_fr,
			__global float * Rn_24h,
			__global float * LE_24h,
			__global float * ET_24h,
			int idx)
{

	z0m[idx] = exp(-5.809f+5.62f*SAVI);

	/* Classification */
	bool I_snow = (NDVI<0.0f) && (albedo>0.47f);
	bool I_water = (NDVI==-1.0f);
	
/*	% NOTE: esat_WL is only used for the wet-limit. To get a true upperlimit for the sensible heat
	% the Landsurface Temperature is used as a proxy instead of air temperature.
	%% Net Radiation */
	float SWnet = (1.0f - albedo) * SWd; /* Shortwave Net Radiation [W/m2] */
	float LWnet = emissivity*LWd - emissivity*Sigma_SB*LST_K*LST_K*LST_K*LST_K; /* Longwave Net Radiation [W/m2] */
	Rn[idx] = SWnet+LWnet; /* Total Net Radiation [W/m2] */
	
	/* Ground Heat Flux */
	/* Kustas et al 1993 */
	/* Kustas, W.P., Daughtry, C.S.T. van Oevelen P.J., 
	Analatytical Treatment of Relationships between Soil heat flux/net radiation and Vegetation Indices, 
	Remote sensing of environment,46:319-330 (1993) */
	G0[idx] = Rn[idx] * (((LST_K-T0)/albedo)*(0.0038f*albedo+0.0074*albedo*albedo)*(1.0f-0.98f*NDVI*NDVI*NDVI*NDVI)); 

	if (I_water || I_snow)
	{
		G0[idx]= 0.3f* Rn[idx]; 
	}
	
	U_star[idx] = k*Uref/log(z200/ z0m[idx]);
	
	r_ah[idx] = log(z2/z1)/(U_star[idx]*k);
	
	H[idx] = p*cp*(b+a*(LST_K - T0))/ r_ah[idx];

	LE[idx] = Rn[idx] - H[idx]	- G0[idx];
	
	/* Evaporative fraction */
	evap_fr[idx] = 0.0f;
	if ((Rn[idx] - G0[idx]) != 0.0f)
	{
		evap_fr[idx] = LE[idx]/(Rn[idx]-G0[idx]); /* evaporative fraction [] */
	}
	else
	{
		evap_fr[idx] = 1.0f; /* evaporative fraction upper limit [] (for negative available energy) */
	}

	Rn_24h[idx] = Rg_24h*(1.0f-albedo) - 110*Tao_24h;
	LE_24h[idx] = evap_fr[idx] * Rn_24h[idx];

	ET_24h[idx] = (evap_fr[idx] * Rn_24h[idx]*86.4f)/2450.0f;
	
}


__kernel void SEBAL_EnergyBalance_Kernel(
			__global int * comptMask,
			__global float * SWd,
			__global float * LWd,
			__global float * albedo,
			__global float * emissivity,
			__global float * LST_K,
			__global float * NDVI,
			__global float * Uref,
			__global float * SAVI,
			__global float * a,
			__global float * b,
			__global float * Rg_24h,
			__global float * Tao_24h,

			__global float * z0m,
			__global float * Ustar,
			__global float * r_ah,
			__global float * Rn,
			__global float * G0,
			__global float * H,
			__global float * LE,
			__global float * evap_fr,
			__global float * Rn_24h,
			__global float * LE_24h,
			__global float * ET_24h,
			__global int * DataSize)
{
	int idx = get_global_id(0);

	if(idx<DataSize[0]){
		z0m[idx]=-9999.0f;
		Ustar[idx]=-9999.0f;
		r_ah[idx]=-9999.0f;
		Rn[idx]=-9999.0f;
		G0[idx]=-9999.0f;
		H[idx]=-9999.0f;
		LE[idx]=-9999.0f;
		evap_fr[idx]=-9999.0f;
		Rn_24h[idx]=-9999.0f;
		LE_24h[idx]=-9999.0f;
		ET_24h[idx]=-9999.0f;

		if(comptMask[idx] == 1){
			SEBAL_EnergyBalance_G(
				SWd[idx],
				LWd[idx],
				albedo[idx],
				emissivity[idx],
				LST_K[idx],
				NDVI[idx],
				Uref[idx],
				SAVI[idx],
				a[0],
				b[0],
				Rg_24h[idx],
				Tao_24h[idx],
				z0m,
				Ustar,
				r_ah,
				Rn,
				G0,
				H,
				LE,
				evap_fr,
				Rn_24h,
				LE_24h,
				ET_24h,
				idx);
		}
	}
}

