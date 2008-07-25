
import itkExtras

class bioformats( itkExtras.pipeline ):
  """ Use bioformat to import image in ITK.
  """
  def __init__(self, FileName=None, Channel=0, Series=0, ImageType=None ):
    import itk
    itk.pipeline.__init__(self)
    
    # set a fake name to avoid running the filter with the wrong channel
    self.SetFileName( False )
    
    # if ImageType is None, give it a default value
    # this is useful to avoid loading Base while loading this module
    if ImageType == None:
      ImageType = itk.Image.UC3
      
    # remove useless SetInput() method created by the constructor of the pipeline class
#     del self.SetInput

    # use the tempfile module to get a non used file name and to put
    # the file at the rignt place
    import tempfile
    self.__tmpFile__ = tempfile.NamedTemporaryFile(suffix='.tif')
    
    # set up the pipeline
    self.connect( itk.ImageFileReader[ImageType].New(FileName=self.__tmpFile__.name) )
    self.connect( itk.ChangeInformationImageFilter[ImageType].New( ChangeSpacing=True ) )
    
    # and configure the pipeline
    self.SetChannel( Channel )
    self.SetSeries( Series )
    self.SetFileName( FileName )
      
  def Run(self):
    if self.GetFileName():
      # found the dir where the needed files are
      import os
      dir = os.path.dirname(__file__)+os.sep+"bioformats"
      cp = "%s%sbio-formats.jar:%s%sloci_tools.jar:%s" % (dir, os.sep, dir, os.sep, dir)
      # prepare the command
      import commands
      com = "java -cp %s SimpleImageConverter -channel %s -series %s %s %s"
      com = com % (cp, self.GetChannel(), self.GetSeries(), self.GetFileName(), self.__tmpFile__.name)
      # print com
      status, output = commands.getstatusoutput( com )
      if status:
        raise output
      spacing = [float(v) for v in output.strip().split("\t")]
      self[-1].SetOutputSpacing( spacing )
    
  def SetFileName( self, fileName ):
    self.__file_name__ = fileName
    self.Run()

  def SetChannel( self, channel ):
    self.__channel__ = channel
    self.Run()

  def SetSeries( self, series ):
    self.__series__ = series
    self.Run()

  def GetFileName(self):
    return self.__file_name__
  
  def GetChannel(self):
    return self.__channel__

  def GetSeries(self):
    return self.__series__


del itkExtras
