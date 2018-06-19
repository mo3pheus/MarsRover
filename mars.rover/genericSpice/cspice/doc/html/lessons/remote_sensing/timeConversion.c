#include <stdio.h>
#include "SpiceUsr.h"

#define METAKAR "remoteSensingMetaKernel.mkl"
#define SCLKID -82
#define STRLEN 50

int main(void){
	SpiceChar userUTCTime[STRLEN];

	SpiceDouble et;
	SpiceChar spaceClockTime[STRLEN];
	SpiceChar utctime[STRLEN];
	SpiceChar calTime[STRLEN];

	printf("Please enter UTC Time:\n");
	scanf("%s", userUTCTime);
	printf("You entered %s\n", userUTCTime);


	/* I need to use the following kernel files
		1. naif0008.tls LSK generic kernel
		2. cas00084.tsc SCLK Cassini SCLK
	*/
	furnsh_c( METAKAR );

	str2et_c( userUTCTime, &et);
	printf("Converted to et it is :: %lf\n", et);

	etcal_c( et, STRLEN, calTime );
	printf("Converted to calTime it is %s\n", calTime);

	timout_c( et, "YYYY-MON-DDTHR:MN:SC :: TDB", STRLEN, calTime);
	printf("Converted to timeOut it is %s\n", calTime);

	sce2s_c( SCLKID, et, STRLEN, spaceClockTime );
	printf("Space Craft Clock Time is %s\n", spaceClockTime);

	return(0);
}
