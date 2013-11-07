#include "Constants.h"

extern "C"{

 __device__ void SEBAL_EnergyBalance_G(
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

			float * z0m,
			float * U_star,
			float * r_ah,
			float * Rn,
			float * G0,
			float * H,
			float * LE,
			float * evap_fr,
			float * Rn_24h,
			float * LE_24h,
			float * ET_24h)
{

	*z0m = expf(-5.809f+5.62f*SAVI);

	/* Classification */
	bool I_snow = (NDVI<0.0f) && (albedo>0.47f);
	bool I_water = (NDVI==-1.0f);
	
/*	% NOTE: esat_WL is only used for the wet-limit. To get a true upperlimit for the sensible heat
	% the Landsurface Temperature is used as a proxy instead of air temperature.
	%% Net Radiation */
	float SWnet = (1.0f - albedo) * SWd; /* Shortwave Net Radiation [W/m2] */
	float LWnet = emissivity*LWd - emissivity*Sigma_SB*LST_K*LST_K*LST_K*LST_K; /* Longwave Net Radiation [W/m2] */
	*Rn = SWnet+LWnet; /* Total Net Radiation [W/m2] */
	
	/* Ground Heat Flux */
	/* Kustas et al 1993 */
	/* Kustas, W.P., Daughtry, C.S.T. van Oevelen P.J., 
	Analatytical Treatment of Relationships between Soil heat flux/net radiation and Vegetation Indices, 
	Remote sensing of environment,46:319-330 (1993) */
	*G0 = *Rn * (((LST_K-T0)/albedo)*(0.0038f*albedo+0.0074*albedo*albedo)*(1.0f-0.98f*NDVI*NDVI*NDVI*NDVI)); 

	if (I_water || I_snow)
	{
		*G0= 0.3f* *Rn; 
	}
	
	*U_star = k*Uref/logf(z200/ *z0m);
	
	*r_ah = logf(z2/z1)/(*U_star*k);
	
	*H = p*cp*(b+a*(LST_K - T0))/ *r_ah;

	*LE = *Rn - *H	- *G0;
	
	/* Evaporative fraction */
	*evap_fr = 0.0f;
	if ((*Rn - *G0) != 0.0f)
	{
		*evap_fr = *LE/(*Rn-*G0); /* evaporative fraction [] */
	}
	else
	{
		*evap_fr = 1.0f; /* evaporative fraction upper limit [] (for negative available energy) */
	}

	*Rn_24h = Rg_24h*(1-albedo) - 110.0f*Tao_24h;
	*LE_24h = *evap_fr * *Rn_24h;

	*ET_24h = (*evap_fr * *Rn_24h*86.4f)/2450.0f;
}


 __global__ void SEBAL_EnergyBalance_Kernel(
			int * comptMask,
			float * SWd,
			float * LWd,
			float * albedo,
			float * emissivity,
			float * LST_K,
			float * NDVI,
			float * Uref,
			float * SAVI,
			float * a,
			float * b,
			float * Rg_24h,
			float * Tao_24h,

			float * z0m,
			float * Ustar,
			float * r_ah,
			float * Rn,
			float * G0,
			float * H,
			float * LE,
			float * evap_fr,
			float * Rn_24h,
			float * LE_24h,
			float * ET_24h,
			int  DataSize)
{
	int idx = blockIdx.x*blockDim.x + threadIdx.x;

	if(idx<DataSize){
		*(z0m+idx)=-9999.0f;
		*(Ustar+idx)=-9999.0f;
		*(r_ah+idx)=-9999.0f;
		*(Rn+idx)=-9999.0f;
		*(G0+idx)=-9999.0f;
		*(H+idx)=-9999.0f;
		*(LE+idx)=-9999.0f;
		*(evap_fr+idx)=-9999.0f;
		*(Rn_24h+idx)=-9999.0f;
		*(LE_24h+idx)=-9999.0f;
		*(ET_24h+idx)=-9999.0f;

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
				(z0m+idx),
				(Ustar+idx),
				(r_ah+idx),
				(Rn+idx),
				(G0+idx),
				(H+idx),
				(LE+idx),
				(evap_fr+idx),
				(Rn_24h+idx),
				(LE_24h+idx),
				(ET_24h+idx));
		}
	}
}

}
