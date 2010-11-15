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


import java.io.IOException;
import loci.formats.*;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import java.io.FileOutputStream;

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
  public static boolean testConvert(String[] args)
    throws FormatException, IOException
  {
    String in = null, out = null;
    int series = 0;
    int channel = 0;
    int time = 0;
    boolean usetime = false;
    int zposition = 0;
    boolean usez = false;
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
              usetime = true;
            }
            catch (NumberFormatException exc) { }
          }
          else if (args[i].equals("-z")) {
            try {
              zposition = Integer.parseInt(args[++i]);
              usez = true;
            }
            catch (NumberFormatException exc) { }
          }
          else System.out.println("Ignoring unknown command flag: " + args[i]);
        }
        else {
          if (in == null) in = args[i];
          else if (out == null) out = args[i];
          else System.out.println("Ignoring unknown argument: " + args[i]);
        }
      }
    }
    if (in == null || out == null) {
      System.out.println("  bfconvert [-debug] in_file out_file");
      return false;
    }

    long start = System.currentTimeMillis();
    // System.out.print(in + " ");
    IFormatReader reader = new ImageReader();
    reader = new ChannelSeparator(reader);
    
    
    reader.setMetadataFiltered(true);
    reader.setOriginalMetadataPopulated(true);
    MetadataStore store = MetadataTools.createOMEXMLMetadata();
    if (store == null) System.out.println("OME-Java library not found.");
    else reader.setMetadataStore(store);

    reader.setId(in);
    reader.setSeries(series);
    
    System.out.println( reader.getSizeC() );
    System.out.println( reader.getSizeT() );
    System.out.println( reader.getSeriesCount() );
    System.out.println( reader.getSizeZ() );

    FileOutputStream writer = new FileOutputStream(out);
    store = reader.getMetadataStore();
    writer.write("NRRD0004\n".getBytes());
    writer.write("type: ".getBytes());
    int pt = reader.getPixelType();
    switch( reader.getPixelType() ) {
     case FormatTools.INT8: writer.write("int8".getBytes()); break;
     case FormatTools.INT16: writer.write("int16".getBytes()); break;
     case FormatTools.INT32: writer.write("int32".getBytes()); break;
     case FormatTools.UINT8: writer.write("uint8".getBytes()); break;
     case FormatTools.UINT16: writer.write("uint16".getBytes()); break;
     case FormatTools.UINT32: writer.write("uint32".getBytes()); break;
     case FormatTools.FLOAT: writer.write("float".getBytes()); break;
     case FormatTools.DOUBLE: writer.write("double".getBytes()); break;
     default: System.out.println("OME-Java library not found."); return false;
     }
    writer.write("\n".getBytes());
    writer.write("dimension: 3\n".getBytes());
    writer.write("space: left-posterior-superior\n".getBytes());
    writer.write("kinds: domain domain domain\n".getBytes());
    writer.write("encoding: raw\n".getBytes());
    writer.write("space origin: (0,0,0)\n".getBytes());


    if( usez && usetime )
      {
      writer.write(("sizes: "+reader.getSizeX()+" "+reader.getSizeY()+" 1\n").getBytes());
      if (store instanceof MetadataRetrieve) {
        MetadataRetrieve meta = (MetadataRetrieve) store;
        writer.write(("space directions: ("+meta.getPixelsPhysicalSizeX(0)+",0,0) (0,"+meta.getPixelsPhysicalSizeY(0)+",0) (0,0,1.0)\n").getBytes());
      }
      writer.write("\n".getBytes());
      byte[] image = reader.openBytes( reader.getIndex(zposition, channel, time) );
      writer.write(image);
//       writer.saveBytes(0, image);
      }
    else if( usetime && !usez )
      {
      writer.write(("sizes: "+reader.getSizeX()+" "+reader.getSizeY()+" "+reader.getSizeZ()+"\n").getBytes());
      if (store instanceof MetadataRetrieve) {
        MetadataRetrieve meta = (MetadataRetrieve) store;
        writer.write(("space directions: ("+meta.getPixelsPhysicalSizeX(0)+",0,0) (0,"+meta.getPixelsPhysicalSizeY(0)+",0) (0,0,"+meta.getPixelsPhysicalSizeZ(0)+")\n").getBytes());
      } 
      writer.write("\n".getBytes());
      for( int z=0; z<reader.getSizeZ(); z++ )
        {
  //      System.out.println(z);
        byte[] image = reader.openBytes( reader.getIndex(z, channel, time) );
        writer.write(image);
//         writer.saveBytes(z, image);
        }
      }
    else if( usez && !usetime )
      {
      writer.write(("sizes: "+reader.getSizeX()+" "+reader.getSizeY()+" "+reader.getSizeT()+"\n").getBytes());
      if (store instanceof MetadataRetrieve) {
        MetadataRetrieve meta = (MetadataRetrieve) store;
        writer.write(("space directions: ("+meta.getPixelsPhysicalSizeX(0)+",0,0) (0,"+meta.getPixelsPhysicalSizeY(0)+",0) (0,0,"+meta.getPixelsTimeIncrement(0)+")\n").getBytes());
      } 
      writer.write("\n".getBytes());
      for( int t=0; t<reader.getSizeT(); t++ )
        {
  //      System.out.println(z);
        byte[] image = reader.openBytes( reader.getIndex(zposition, channel, t) );
        writer.write(image);
//         writer.saveBytes(t, image);
        }
      }
    writer.close();
    return true;
  }

  // -- Main method --

  public static void main(String[] args) throws FormatException, IOException {
    if (!testConvert(args)) System.exit(1);
  }

}
