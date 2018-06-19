/*******************************************************************************************************
*	Name			: MSLSclkUtil.c
*	argv[1]			: String representing UTC Time e.g. 2017-09-31-15:32:00
*	returns			: SCLK time if range is valid.
*	returns			: -1 if time is out of scope for provided sclk
*
*	NOTE: THIS CODE WORKS ONLY FOR MSL.
*
*******************************************************************************************************/

/******************************************************************************************************
*	Includes Section
******************************************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include "SpiceUsr.h"

/*****************************************************************************************************
*	Defines Section
*****************************************************************************************************/
#define SEPARATOR "====================================================================================\n"
#define LEAP_SECONDS_KERNEL "dependencies/naif0012.tls"
#define SCLK_KERNEL "dependencies/msl_lmst_ops120808_v1.tsc"
#define STR_LEN 50

/* Main Call 
	argv[0] -> name of the program
	argv[1] -> user entered UTC time.
*/
void main(int argc, char** argv)
	{
		/* Declare variables */
		SpiceChar userUTCTime[STR_LEN];			/* user entered UTC Time	   */
		SpiceDouble et,spacecraftTime;			/* ephemerisTime, spacecraftTime   */
		static SpiceChar sclkch[100];			/* spacecraftTime String	   */

		int i = 0;
		for(i=0; i < strlen(argv[1]); i++)
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
		Spice Logic starts here.
		------------------------------------------------------------------------------------*/
		furnsh_c(LEAP_SECONDS_KERNEL);
		furnsh_c(SCLK_KERNEL);
		str2et_c(userUTCTime, &et);

		/* Convert et into sclk time */
		//sce2s_c( -76, et, 100,  sclkch);
	        sce2s_c   ( -76900, et, STR_LEN, sclkch );

		SpiceChar calTime[STR_LEN];
		etcal_c(et, STR_LEN, calTime);

		printf("%s,%f,%s", sclkch, et, calTime);
	}
