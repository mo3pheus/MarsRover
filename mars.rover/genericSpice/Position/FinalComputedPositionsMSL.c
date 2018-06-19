/**********************************************************************************************
*       INCLUDES SECTIN
**********************************************************************************************/
#include <stdio.h>
#include "SpiceUsr.h"
#include <string.h>

#define GENERIC_LSK "dependencies/naif0012.tls"
#define CURIOSITY_SCLK "dependencies/msl_lmst_ops120808_v1.tsc"
#define CURIOSITY_SCLK_2 "dependencies/msl_76_sclkscet_refit_n4.tsc"
#define MERGED_MISSION_DATA_SPK "dependencies/mergedCuriosityData.spk"
#define SOLAR_SYSTEM_EPHEMERIS_SPK "dependencies/981005_PLTEPH-DE405S.bsp"
#define CURIOSITY_FRAMES_KERNEL "dependencies/msl_v08.tf"
#define CURIOSITY_PCK "dependencies/pck00008.tpc"

#define CURIOSITY_CK_KERNEL_1	"dependencies/msl_surf_hga_tlm_1159_1293_v1.bc"
#define CURIOSITY_CK_KERNEL_2 "dependencies/msl_surf_hga_tlm_1293_1417_v1.bc"
#define CURIOSITY_CK_KERNEL_3 "dependencies/msl_surf_hga_tlm_1417_1514_v1.bc"
#define CURIOSITY_CK_KERNEL_4 "dependencies/msl_surf_hga_tlm_1514_1648_v1.bc"

#define CURIOSITY_ROVER_CK_1 "dependencies/msl_surf_rover_tlm_1159_1293_v1.bc"
#define CURIOSITY_ROVER_CK_2 "dependencies/msl_surf_rover_tlm_1293_1417_v1.bc"
#define CURIOSITY_ROVER_CK_3 "dependencies/msl_surf_rover_tlm_1417_1514_v1.bc"
#define CURIOSITY_ROVER_CK_4 "dependencies/msl_surf_rover_tlm_1514_1648_v1.bc"

#define STR_LEN 50
#define SEPARATOR "\n=======================================================================\n"
#define CURIOSITY -76
#define CURIOSITY_HGA -76122


void main(int argc, char** argv){
	/*--------------------------------------------------------------------------------------------
	*	Load the required kernels
	--------------------------------------------------------------------------------------------*/
	furnsh_c(GENERIC_LSK);
	furnsh_c(CURIOSITY_SCLK);
	furnsh_c(CURIOSITY_SCLK_2);
	furnsh_c(MERGED_MISSION_DATA_SPK);
	furnsh_c(SOLAR_SYSTEM_EPHEMERIS_SPK);
	furnsh_c(CURIOSITY_FRAMES_KERNEL);
	furnsh_c(CURIOSITY_PCK);
	furnsh_c(CURIOSITY_CK_KERNEL_1);
	furnsh_c(CURIOSITY_CK_KERNEL_2);
	furnsh_c(CURIOSITY_CK_KERNEL_3);
	furnsh_c(CURIOSITY_CK_KERNEL_4);
	furnsh_c(CURIOSITY_ROVER_CK_1);
	furnsh_c(CURIOSITY_ROVER_CK_2);
	furnsh_c(CURIOSITY_ROVER_CK_3);
	furnsh_c(CURIOSITY_ROVER_CK_4);

	/*-------------------------------------------------------------------------------------------
	* Variable declarations
	--------------------------------------------------------------------------------------------*/
	SpiceChar userUTCTime[STR_LEN];                 /* user entered UTC Time           */
        SpiceDouble et,spacecraftTime;                  /* ephemerisTime, spacecraftTime   */
	SpiceDouble lightTimeMSLEarth;			/* one way light time earth to msl */
	static SpiceChar sclkch[100];                   /* spacecraftTime String           */
	SpiceDouble bfixst[6];				/* antenna boresight vector        */
	SpiceDouble stateCuriosity[6];			/* state vector for curiosity      */
	SpiceDouble stateEarth[6];			/* state vector for earth          */
	SpiceDouble posnCuriosity[3];			/* position of Curiosity 	   */
	SpiceDouble xFormMatrix[6][6];
	SpiceDouble xformHG_J2000[3][3];
	SpiceDouble posEarthCuriosity[3];
	SpiceDouble bsight[3];
	SpiceDouble angularSeparation;

        /* Check if arguments have been passed */
         int i = 0;
	 int argumentLength = strlen(argv[1]);
         for(i=0; i < argumentLength; i++)
	         {
        	 	if(argv[1][i] == '~')
                        {
                                userUTCTime[i] = 32;
                        }
                        else
                        {
                                userUTCTime[i] = argv[1][i];
                        }
                }

         /* Adding /n */
         userUTCTime[++i] = 92;
         userUTCTime[++i] = 110;

	/*------------------------------------------------------------------------------------
        * Get the ephemeris time
        ------------------------------------------------------------------------------------*/
	str2et_c( userUTCTime, &et);

         /*------------------------------------------------------------------------------------
         * Find the state vector for Curiosity
         ------------------------------------------------------------------------------------*/
	spkezr_c( "MSL", et, "J2000", "LT+S", "EARTH", stateCuriosity, &lightTimeMSLEarth );
	/*printf("\n State Vector for Curiosity as seen from Earth at above mentioned ephemerisTime\n ");
        printf(  "\n      X = %16.3f\n", stateCuriosity[0]       );
        printf ( "      Y = %16.3f\n", stateCuriosity[1]       );
        printf ( "      Z = %16.3f\n", stateCuriosity[2]       );
        printf ( "     VX = %16.3f\n", stateCuriosity[3]       );
        printf ( "     VY = %16.3f\n", stateCuriosity[4]       );
        printf ( "     VZ = %16.3f\n", stateCuriosity[5]       );

	printf("\nLight time between Earth and Curiosity at %f :: is %f ", et, lightTimeMSLEarth );
	printf(SEPARATOR);*/

	/*------------------------------------------------------------------------------------
         * Find the state vector for Earth
         ------------------------------------------------------------------------------------*/
	double lightTimeEarthMSL_state = 0.0;
	spkezr_c( "EARTH", et, "J2000", "LT+S", "MSL", stateEarth, &lightTimeEarthMSL_state );
	/*printf("\n State Vector for Earth as seen from Curiosity at above mentioned ephemerisTime\n ");
        printf(  "\n      X = %16.3f\n", stateEarth[0]       );
        printf ( "      Y = %16.3f\n", stateEarth[1]       );
        printf ( "      Z = %16.3f\n", stateEarth[2]       );
        printf ( "     VX = %16.3f\n", stateEarth[3]       );
        printf ( "     VY = %16.3f\n", stateEarth[4]       );
        printf ( "     VZ = %16.3f\n", stateEarth[5]       );

	printf("\nLight time between Earth and Curiosity at %f :: is %f ", et, lightTimeEarthMSL_state );
	printf(SEPARATOR);*/

	
	/*------------------------------------------------------------------------------------
         * Find the position of Earth wrt Curiosity in the J2000 frame
         ------------------------------------------------------------------------------------*/
	double lightTimeEarthMSL;
	spkpos_c("EARTH", et, "J2000", "LT+S", "MSL", posEarthCuriosity, & lightTimeEarthMSL);
	/*printf(SEPARATOR);
	printf("\n Position Vector for Earth as seen from Curiosity at above mentioned ephemerisTime\n ");
        printf(  "\n      X = %16.3f\n", posEarthCuriosity[0]       );
        printf ( "      Y = %16.3f\n", posEarthCuriosity[1]       );
        printf ( "      Z = %16.3f\n", posEarthCuriosity[2]       );
	printf("\n Light time between Earth and Curiosity %f", lightTimeEarthMSL);
        printf(SEPARATOR);*/

	/*------------------------------------------------------------------------------------
        * From the Curiosity frames kernel we know that the HG Antenna is aligned with the
	* z-axis of the MSL_HGA frame
        ------------------------------------------------------------------------------------*/
	bsight[0] = 0.0;
	bsight[1] = 0.0;
	bsight[2] = 1.0;
	
	/*------------------------------------------------------------------------------------
        * Compute the rotation matrix from Curiosity HGA frame to J2000 frame
        ------------------------------------------------------------------------------------*/
	pxform_c( "MSL_HGA", "J2000", et, xformHG_J2000);

	/*------------------------------------------------------------------------------------
        * Multiply the result to obtain the nominal antenna boresight in the J2000 frame
        ------------------------------------------------------------------------------------*/
	mxv_c( xformHG_J2000, bsight, bsight );

	/*------------------------------------------------------------------------------------
        * Compute the angular separation in degrees between the aparent position of Earth as
	* seen from Curiosity at et, in the J2000 frame with the Curiosity HGA boresight
        ------------------------------------------------------------------------------------*/
	convrt_c( vsep_c( bsight, posEarthCuriosity ), "RADIANS", "DEGREES", &angularSeparation );
	/*printf(" Angular separation between the apparent position of Earth and the\n CURIOSITY high gain antenna boresight (degrees) :\n" );
	printf(" %16.3f\n", angularSeparation);*/

	/*-----------------------------------------------------------------------------------
	* Get the sclk string as well 
	-----------------------------------------------------------------------------------*/
	sce2s_c   ( -76900, et, STR_LEN, sclkch );

	//et, stC0, stC1, stC2, stC3, stC4, stC5, ltCE, stE0, stE1, stE2, stE3, stE4, stE5, ltEC, posEC0, posEC1, posEC2, ltEC, angularSeparation
	printf("%f,%16.3f,%16.3f,%16.3f,%16.3f,%16.3f,%16.3f,%16.3f,%16.3f,%16.3f,%16.3f,%16.3f,%16.3f, %16.3f, %16.3f, %16.3f,%16.3f,%16.3f, %16.3f, %16.3f, %s\n", et, stateCuriosity[0], stateCuriosity[1], stateCuriosity[2], stateCuriosity[3], stateCuriosity[4], stateCuriosity[5], lightTimeMSLEarth, stateEarth[0], stateEarth[1], stateEarth[2], stateEarth[3], stateEarth[4], stateEarth[5], lightTimeEarthMSL_state, posEarthCuriosity[0], posEarthCuriosity[1], posEarthCuriosity[2], lightTimeEarthMSL, angularSeparation, sclkch);

}
