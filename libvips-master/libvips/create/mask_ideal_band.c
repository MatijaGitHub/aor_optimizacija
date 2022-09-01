/* creates an ideal filter.
 *
 * 02/01/14
 * 	- from ideal.c
 */

/*

    This file is part of VIPS.
    
    VIPS is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
    02110-1301  USA

 */

/*

    These files are distributed with VIPS - http://www.vips.ecs.soton.ac.uk

 */

/*
#define VIPS_DEBUG
 */

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif /*HAVE_CONFIG_H*/
#include <glib/gi18n-lib.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>

#include <vips/vips.h>

#include "pcreate.h"
#include "point.h"
#include "pmask.h"

typedef struct _VipsMaskIdealBand {
	VipsMask parent_instance;

	double frequency_cutoff_x;
	double frequency_cutoff_y;
	double radius;

} VipsMaskIdealBand;

typedef VipsMaskClass VipsMaskIdealBandClass;

G_DEFINE_TYPE( VipsMaskIdealBand, vips_mask_ideal_band, 
	VIPS_TYPE_MASK );

static double
vips_mask_ideal_band_point( VipsMask *mask, double dx, double dy ) 
{
	VipsMaskIdealBand *ideal_band = (VipsMaskIdealBand *) mask;
	double fcx = ideal_band->frequency_cutoff_x;
	double fcy = ideal_band->frequency_cutoff_y;
	double r2 = ideal_band->radius * ideal_band->radius;

	double d1 = (dx - fcx) * (dx - fcx) + (dy - fcy) * (dy - fcy);
	double d2 = (dx + fcx) * (dx + fcx) + (dy + fcy) * (dy + fcy);

	return( (d1 < r2 || d2 < r2) ? 1.0 : 0.0 ); 
}

static void
vips_mask_ideal_band_class_init( VipsMaskIdealBandClass *class )
{
	GObjectClass *gobject_class = G_OBJECT_CLASS( class );
	VipsObjectClass *vobject_class = VIPS_OBJECT_CLASS( class );
	VipsMaskClass *mask_class = VIPS_MASK_CLASS( class );

	gobject_class->set_property = vips_object_set_property;
	gobject_class->get_property = vips_object_get_property;

	vobject_class->nickname = "mask_ideal_band";
	vobject_class->description = _( "make an ideal band filter" );

	mask_class->point = vips_mask_ideal_band_point;

	VIPS_ARG_DOUBLE( class, "frequency_cutoff_x", 6, 
		_( "Frequency cutoff x" ), 
		_( "Frequency cutoff x" ),
		VIPS_ARGUMENT_REQUIRED_INPUT,
		G_STRUCT_OFFSET( VipsMaskIdealBand, frequency_cutoff_x ),
		0.0, 1000000.0, 0.5 );

	VIPS_ARG_DOUBLE( class, "frequency_cutoff_y", 7, 
		_( "Frequency cutoff y" ), 
		_( "Frequency cutoff y" ),
		VIPS_ARGUMENT_REQUIRED_INPUT,
		G_STRUCT_OFFSET( VipsMaskIdealBand, frequency_cutoff_y ),
		0.0, 1000000.0, 0.5 );

	VIPS_ARG_DOUBLE( class, "radius", 8, 
		_( "Radius" ), 
		_( "Radius of circle" ),
		VIPS_ARGUMENT_REQUIRED_INPUT,
		G_STRUCT_OFFSET( VipsMaskIdealBand, radius ),
		0.0, 1000000.0, 0.1 );

}

static void
vips_mask_ideal_band_init( VipsMaskIdealBand *ideal_band )
{
	ideal_band->frequency_cutoff_x = 0.5;
	ideal_band->frequency_cutoff_y = 0.5;
	ideal_band->radius = 0.1;
}

/**
 * vips_mask_ideal_band:
 * @out: (out): output image
 * @width: image size
 * @height: image size
 * @frequency_cutoff_x: position of band
 * @frequency_cutoff_y: position of band
 * @radius: size of band
 * @...: %NULL-terminated list of optional named arguments
 *
 * Optional arguments:
 *
 * * @nodc: don't set the DC pixel
 * * @reject: invert the filter sense
 * * @optical: coordinates in optical space
 * * @uchar: output a uchar image
 *
 * Make an ideal band-pass or band-reject filter, that is, one with a 
 * sharp cutoff around the point @frequency_cutoff_x, @frequency_cutoff_y, 
 * of size @radius. 
 *
 * See also: vips_mask_ideal().
 *
 * Returns: 0 on success, -1 on error
 */
int
vips_mask_ideal_band( VipsImage **out, int width, int height, 
	double frequency_cutoff_x, double frequency_cutoff_y, 
	double radius, ... )
{
	va_list ap;
	int result;

	va_start( ap, radius );
	result = vips_call_split( "mask_ideal_band", ap, out, width, height, 
		frequency_cutoff_x, frequency_cutoff_y, radius );
	va_end( ap );

	return( result );
}
