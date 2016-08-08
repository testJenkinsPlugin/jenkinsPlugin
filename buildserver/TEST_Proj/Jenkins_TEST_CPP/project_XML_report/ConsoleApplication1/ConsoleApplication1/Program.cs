using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using System.IO;

namespace ConsoleApplication1
{
    class Program
    {
        static void Main(string[] args)
        {
            string path = @"Data\report.xml";
            string path2 = @"Data\reports\report.xml";
          if(File.Exists(path2))
          {
              try
              {
                  File.Delete(path2);
              }
              catch (Exception e)
              {
                  Console.WriteLine(e.Message);
                  Console.WriteLine("First message");
              }

          }
              XmlDocument doc = new XmlDocument();
              doc.Load(path);
              doc.Save(path2);
          
        }
    }
}
