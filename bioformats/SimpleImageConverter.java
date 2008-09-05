//
// ImageConverter.java
//

/*
OME Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


import java.awt.Image;
import java.io.IOException;
import loci.formats.*;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import loci.formats.out.TiffWriter;

/**
 * ImageConverter is a utility class for converting a file between formats.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/loci/formats/tools/ImageConverter.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/loci/formats/tools/ImageConverter.java">SVN</a></dd></dl>
 */
public final class SimpleImageConverter {

  // -- Constructor --

  private SimpleImageConverter() { }

  // -- Utility methods --

  /** A utility method for converting a file from the command line. */
  public static boolean testConvert(IFormatWriter writer, String[] args)
    throws FormatException, IOException
  {
    String in = null, out = null;
    int series = 0;
    int channel = 0;
    int time = 0;
    int zposition = 0;
    boolean dim2 = false;
    if (args != null) {
      for (int i=0; i<args.length; i++) {
        if (args[i].startsWith("-") && args.length > 1) {
          if (args[i].equals("-series")) {
            try {
              series = Integer.parseInt(args[++i]);
            }
            catch (NumberFormatException exc) { }
          }
          else if (args[i].equals("-channel")) {
            try {
              channel = Integer.parseInt(args[++i]);
            }
            catch (NumberFormatException exc) { }
          }
          else if (args[i].equals("-time")) {
            try {
              time = Integer.parseInt(args[++i]);
            }
            catch (NumberFormatException exc) { }
          }
          else if (args[i].equals("-z")) {
            try {
              zposition = Integer.parseInt(args[++i]);
	      dim2 = true;
            }
            catch (NumberFormatException exc) { }
          }
          else LogTools.println("Ignoring unknown command flag: " + args[i]);
        }
        else {
          if (in == null) in = args[i];
          else if (out == null) out = args[i];
          else LogTools.println("Ignoring unknown argument: " + args[i]);
        }
      }
    }
    if (FormatHandler.debug) {
      LogTools.println("Debugging at level " + FormatHandler.debugLevel);
    }
    if (in == null || out == null) {
      LogTools.println("To convert a file to " + writer.getFormat() +
        " format, run:");
      LogTools.println("  bfconvert [-debug] in_file out_file");
      return false;
    }

    long start = System.currentTimeMillis();
    // LogTools.print(in + " ");
    IFormatReader reader = new ImageReader();
    reader = new ChannelSeparator(reader);
    
    
    reader.setMetadataFiltered(true);
    reader.setOriginalMetadataPopulated(true);
    MetadataStore store = MetadataTools.createOMEXMLMetadata();
    if (store == null) LogTools.println("OME-Java library not found.");
    else reader.setMetadataStore(store);

    reader.setId(in);
    reader.setSeries(series);
    
    store = reader.getMetadataStore();
    if (store instanceof MetadataRetrieve) {
      MetadataRetrieve meta = (MetadataRetrieve) store;
      writer.setMetadataRetrieve(meta);
      LogTools.println( meta.getDimensionsPhysicalSizeX(0, 0)+"\t"+meta.getDimensionsPhysicalSizeY(0, 0)+"\t"+meta.getDimensionsPhysicalSizeZ(0, 0) );
      LogTools.println( reader.getSizeC() );
      LogTools.println( reader.getSizeT() );
      LogTools.println( reader.getSeriesCount() );
      LogTools.println( reader.getSizeZ() );
    }

    writer.setId(out);
    
    if( dim2 )
      {
      Image image = reader.openImage( reader.getIndex(zposition, channel, time) );
      writer.saveImage(image, true);
      }
    else
      {
      for( int z=0; z<reader.getSizeZ(); z++ )
	{
  //      LogTools.println(z);
	Image image = reader.openImage( reader.getIndex(z, channel, time) );
	writer.saveImage(image, z==reader.getSizeZ()-1);
	}
      }
    return true;
  }

  // -- Main method --

  public static void main(String[] args) throws FormatException, IOException {
    if (!testConvert(new ImageWriter(), args)) System.exit(1);
  }

}
