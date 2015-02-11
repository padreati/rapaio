/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.core;

import static java.lang.Math.sqrt;

/**
 * Class defining constants.
 */

public class Constants { 

	/* 30 Decimal-place constants computed with bc -l (scale=32; proper round) */

	public static final double  M_SQRT_2 = 1.41421356237309504880168872420969807856967187537694807317667973799073247846210703885038753432764157273501384623091229702; 
	/* 1/sqrt(2) */
	public static final double  M_1_SQRT_2 = 0.70710678118654752440084436210484903928483593768847403658833986899536623923105351942519376716382078636750692311545614851; 
	/* sqrt(32) */
	public static final double  M_SQRT_32 = 5.65685424949238019520675489683879231427868750150779229270671895196292991384842815540155013731056629094005538492364918809; 

	public static final double  M_LN2 = 0.69314718055994530941723212145817656807550013436025525412068000949339362196969471560586332699641868754200148102057068573; 
	public static final double  M_LN10 = 2.30258509299404568401799145468436420760110148862877297603332790096757260967735248023599720508959829834196778404228624863; 
	public static final double  M_LOG10_2 = 0.30102999566398119521373889472449302676818988146210854131042746112710818927442450948692725211818617204068447719143099537; 

	public static final double  M_PI   = 3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664; 
	public static final double  M_2PI = 6.28318530717958647692528676655900576839433879875021164194988918461563281257241799725606965068423413596429617302656461329;
	public static final double  M_LOG_PI = 1.14472988584940017414342735135305871164729481291531157151362307147213776988482607978362327027548970770200981222869798915;

	/* 1/pi */
	public static final double  M_1_PI =  0.31830988618379067153776752674502872406891929148091289749533468811779359526845307018022760553250617191214568545351591607;

	/* pi/2 */
	public static final double  M_PI_2 =  1.57079632679489661923132169163975144209858469968755291048747229615390820314310449931401741267105853399107404325664115332;

	public static final double M_LN_2PI = 1.83787706640934548356065947281123527972279494727556682563430308096553139185452079538948659727190839524401129324926867489;

	/* sqrt(pi),  1/sqrt(2pi),  sqrt(2/pi) : */
	public static final double  M_SQRT_PI = 1.77245385090551602729816748334114518279754945612238712821380778985291128459103218137495065673854466541622682362428257066; 
	public static final double  M_1_SQRT_2PI = 0.39894228040143267793994605993438186847585863116493465766592582967065792589930183850125233390730693643030255886263518268; 
	public static final double  M_SQRT_2dPI = 0.79788456080286535587989211986876373695171726232986931533185165934131585179860367700250466781461387286060511772527036537; 

	/* log(sqrt(pi)) = log(pi)/2 : */
	public static final double  M_LN_SQRT_PI = 0.57236494292470008707171367567652935582364740645765578575681153573606888494241303989181163513774485385100490611434899457; 
	/* log(sqrt(2*pi)) = log(2*pi)/2 : */
	public static final double  M_LN_SQRT_2PI = 0.91893853320467274178032973640561763986139747363778341281715154048276569592726039769474329863595419762200564662463433744; 
	/* log(sqrt(pi/2)) = log(pi/2)/2 : */
	public static final double  M_LN_SQRT_PId2 = 0.22579135264472743236309761494744107178589733927752815869647153098937207395756568208887997163953551008000416560406365171; 

	public static final double  ME_NONE = 0; 
	public static final double  ME_DOMAIN = 1; 
	public static final double  ME_RANGE = 2; 
	public static final double  ME_NOCONV = 3; 
	public static final double  ME_PRECISION = 4; 
	public static final double  ME_UNDERFLOW = 5; 

	/* constants taken from float.h for gcc 2.90.29 for Linux 2.0 i386  */
	/* -- should match Java since both are supposed to be IEEE 754 compliant */

	/* Radix of exponent representation */
	public static final int    FLT_RADIX   = 2;

	/* Difference between 1.0 and the minimum float/double greater than 1.0 */
	public static final double FLT_EPSILON = 1.19209290e-07F;
	public static final double DBL_EPSILON = 2.2204460492503131e-16;
	public static final double DBL_MIN = 2.22507385850720138309e-308;
	public static final double DBL_MAX = 1.797693134862315708145e+308;
	public static final double SQRT_DBL_EPSILON = sqrt(DBL_EPSILON); 

	/* Number of decimal digits of precision in a float/double */
	public static final int FLT_DIG = 6;
	public static final int DBL_DIG = 15;          

	/* Number of base-FLT_RADIX digits in the significand of a double */
	public static final int FLT_MANT_DIG = 24;
	public static final int DBL_MANT_DIG = 53;

	/* Minimum int x such that FLT_RADIX**(x-1) is a normalised double */
	public static final int FLT_MIN_EXP = -125;
	public static final int DBL_MIN_EXP = -1021;

	/* Maximum int x such that FLT_RADIX**(x-1) is a representable double */
	public static final int FLT_MAX_EXP = 128;
	public static final int DBL_MAX_EXP = 1024;    

	public static final double d1mach3 = 0.5 * DBL_EPSILON;
	public static final double d1mach4 = DBL_EPSILON;

	/**
	 * This is the squared inverse of the golden ratio ((3 - sqrt(5.0))/ 2).
	 * Used in golden-ratio search.
	 * Somehow inputting the number directly improves accuracy
	 */
	public static final double kInvGoldRatio = 0.38196601125010515179541316563436188227969082019423713786455137729473953718109755029279279581060886251524591192461310824;
}
