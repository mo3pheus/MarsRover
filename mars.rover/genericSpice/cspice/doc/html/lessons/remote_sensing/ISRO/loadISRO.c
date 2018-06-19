#include <stdio.h>
#include "SpiceUsr.h"

#define ISRO_DATA_KER "MCC_MRD_20160922T054701482_D_D32_sv.bsp"

int main(void){
	printf("\n This program will try to load isro data ");
	furnsh_c(ISRO_DATA_KER);
	return (0);
}
