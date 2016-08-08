using System;
using System.IO;
using System.Collections.Generic;
using System.Windows.Forms;
using System.Text;
using System.Text.RegularExpressions;

namespace VersionChanger
{
    class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        public static void Main(string[] args)
        {
            if (args.Length != 0)
            {
                BAT_FileParser(args);
            }
        }

        private static void BAT_FileParser(string[] args)
        {
            string line = args[0].ToString();
            
            char[] delimiterChar = { '&' };
            string[] arguments = line.Split(delimiterChar);

            foreach (string argument in arguments)
            {
                try
                {
                    if (argument.Contains("FileToChange:"))
                    {
                      char[] delimiterChars1 = { '@' };
                      string[] words = argument.Split(delimiterChars1);

                      char[] delimiterChars2 = { ' ' };
                      string[] version = words[2].Split(delimiterChars2);

                      if (Path.GetExtension(words[1]).ToUpper() == ".CS" ||
                          Path.GetExtension(words[1]).ToUpper() == ".VB" ||
                          Path.GetExtension(words[1]).ToUpper() == ".JSL" ||
                          Path.GetExtension(words[1]).ToUpper() == ".CPP")
                          DotNetFileChanger(words[1], version[0], version[1], version[2], version[3]);
                      if (Path.GetExtension(words[1]).ToUpper() == ".H")
                          CPPlusFileChanger(words[1], version[0], version[1], version[2], version[3]);
                    }
                }
                catch (Exception e)
                {
                    // Let the user know what went wrong.
                    Console.WriteLine(e.Message);
                }
            }   
        }

        private static void CPPlusFileChanger(string path, string strMajor, string strMinor, string strBuild, string strRevision)
        {
            try
            {
                using (StreamReader sr = new StreamReader(path))
                {
                    String line;
                    StringBuilder sb = new StringBuilder();

                    bool needReadVersion = false;

                    while ((line = sr.ReadLine()) != null)
                    {
                        if (line.Contains("BEGIN OF THE BUILDS AUTOMATION DEFINES BLOCK"))
                            needReadVersion = true;

                        if (line.Contains("END OF THE BUILDS AUTOMATION DEFINES BLOCK"))
                            needReadVersion = false;

                        if (needReadVersion)
                        {
                            string pattern = null;
                            string string_pattern = null;

                            if (Path.GetExtension(path).ToUpper() == ".H")
                            {
                                pattern = @"\s*#define\s*\w*\s*\d*.\d*.\d*.\d*\s*";
                                string_pattern = "\\s*#define\\s*[\"]\\w*\\s*\\d*.\\d*.\\d*.\\d*[\"]\\s*";
                            }

                            if (Regex.IsMatch(line, pattern) || Regex.IsMatch(line, string_pattern))
                            {
                                char[] delimiterChars1 = { ' ', '"' };
                                string[] words = line.Split(delimiterChars1);

                                char[] delimiterChars2 = { ',', '.' };
                                string[] segments = null;
                                int delimiter = -1;

                                int index = -1;
                                int version = -1;

                                foreach (string word in words)
                                {
                                    index++;

                                    if (0 == word.CompareTo("Version"))
                                        version = index;

                                    if (word.Contains(","))
                                    {
                                        delimiter = 0;
                                        break;
                                    }
                                    else if (word.Contains("."))
                                    {
                                        delimiter = 1;
                                        break;
                                    }
                                }

                                if (-1 == delimiter)
                                    continue;

                                segments = words[index].Split(delimiterChars2[delimiter]);

                                int range = 0;
                                String strMaj = "", strMin = "", strBld = "", strRev = "";

                                try
                                {
                                    strMaj = segments[0];
                                    strMin = segments[1];
                                    strBld = segments[2];
                                    strRev = segments[3];
                                }
                                catch { }

                                int Major = 0, Minor = 0, Build = 0, Revision = 0;
                                try
                                {
                                    Major = Convert.ToInt32(segments[0]);
                                    range = 1;
                                    Minor = Convert.ToInt32(segments[1]);
                                    range = 2;
                                    Build = Convert.ToInt32(segments[2]);
                                    range = 3;
                                    Revision = Convert.ToInt32(segments[3]);
                                    range = 4;
                                }
                                catch { }

                                try
                                {
                                    if (strMajor == "*" && range >= 1)
                                        Major++;

                                    if (strMinor == "*" && range >= 2)
                                        Minor++;

                                    if (strBuild == "*" && range >= 3)
                                        Build++;

                                    if (strRevision == "*" && range == 4)
                                        Revision++;
                                }
                                catch { }

                                if (-1 != version)
                                    words[version] = "\"" + words[version];

                                words[index] = "";

                                if (line.Contains("\"") && -1 == version)
                                    words[index] = "\"";
                                if (range == 1)
                                    words[index] += Major.ToString() + delimiterChars2[delimiter] + strMin;
                                if (range == 2)
                                    words[index] += Major.ToString() + delimiterChars2[delimiter] + Minor.ToString() + delimiterChars2[delimiter] + strBuild;
                                if (range == 3)
                                    words[index] += Major.ToString() + delimiterChars2[delimiter] + Minor.ToString() + delimiterChars2[delimiter] + strBuild + delimiterChars2[delimiter] + strRevision;
                                if (range == 4)
                                    words[index] += Major.ToString() + delimiterChars2[delimiter] + Minor.ToString() + delimiterChars2[delimiter] + strBuild + delimiterChars2[delimiter] + strRevision;
                                if (line.Contains("\""))
                                    words[index] += "\"";

                                line = "";

                                foreach (string word in words)
                                {
                                    if (0 != word.CompareTo(""))
                                    {
                                        if (line.Length > 0)
                                            line += " " + word;
                                        else if (line.Length == 0)
                                            line += word;
                                    }
                                }
                            }
                        }

                        sb.AppendLine(line);
                    }

                    sr.Close();

                    using (StreamWriter outfile = new StreamWriter(path))
                    {
                        outfile.Write(sb.ToString());
                        outfile.Close();
                    }
                }
            }
            catch (Exception e)
            {
                // Let the user know what went wrong.
                Console.WriteLine("The file could not be read:");
                Console.WriteLine(e.Message);
            }
        }
        private static void DotNetFileChanger(string path, string strMajor, string strMinor, string strBuild, string strRevision)
        {
            try
            {
                using (StreamReader sr = new StreamReader(path))
                {
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = sr.ReadLine()) != null)
                    {
                        string assembly_pattern = null;
                        string file_pattern = null;

                        if (Path.GetExtension(path).ToUpper() == ".CS")
                        {
                            assembly_pattern = "\\s*[[]\\s*assembly\\s*:\\s*AssemblyVersion\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[]]\\s*";
                            file_pattern = "\\s*[[]\\s*assembly\\s*:\\s*AssemblyFileVersion\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[]]\\s*";
                        }
                        if (Path.GetExtension(path).ToUpper() == ".VB")
                        {
                            assembly_pattern = "\\s*[<]\\s*Assembly\\s*:\\s*AssemblyVersion\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[>]\\s*";
                            file_pattern = "\\s*[<]\\s*Assembly\\s*:\\s*AssemblyFileVersion\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[>]\\s*";
                        }
                        if (Path.GetExtension(path).ToUpper() == ".JSL")
                        {
                            assembly_pattern = "\\s*[.*]\\s*@assembly\\s*AssemblyVersion\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[.*]\\s*";
                            file_pattern = "\\s*[.*]\\s*@assembly\\s*AssemblyFileVersion\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[.*]\\s*";
                        }
                        if (Path.GetExtension(path).ToUpper() == ".CPP")
                        {
                            assembly_pattern = "\\s*[[]\\s*assembly\\s*:\\s*AssemblyVersionAttribute\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[]]\\s*";
                            file_pattern = "\\s*[[]\\s*assembly\\s*:\\s*AssemblyVersionAttribute\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[]]\\s*";
                        }                        
                        if (Regex.IsMatch(line, assembly_pattern) || Regex.IsMatch(line, file_pattern))
                        {
                            char[] delimiterChars = { '"' };
                            string[] words = line.Split(delimiterChars);

                            char[] delimiterChars2 = { '.' };
                            string[] segments = words[1].Split(delimiterChars2);

                            int range = 0;
                            String strMaj = "", strMin = "", strBld = "", strRev = "";
                            try
                            {
                                strMaj = segments[0];
                                strMin = segments[1];
                                strBld = segments[2];
                                strRev = segments[3];
                            }
                            catch { }
                            int Major = 0, Minor = 0, Build = 0, Revision = 0;
                            try
                            {
                                Major = Convert.ToInt32(segments[0]);
                                range = 1;
                                Minor = Convert.ToInt32(segments[1]);
                                range = 2;
                                Build = Convert.ToInt32(segments[2]);
                                range = 3;
                                Revision = Convert.ToInt32(segments[3]);
                                range = 4;
                            }
                            catch { }

                            try
                            {
                                if (strMajor == "*" && range >=1)
                                    Major++;

                                if (strMinor == "*" && range >= 2)
                                    Minor++;

                                if (strBuild == "*" && range >= 3)
                                    Build++;

                                if (strRevision == "*" && range == 4)
                                    Revision++;
                            }
                            catch { }

                            if (range == 1)
                                words[1] = "\"" + Major.ToString() + "." + strMin + "\"";
                            if (range == 2)
                                words[1] = "\"" + Major.ToString() + "." + Minor.ToString() + "." + strBuild + "\"";
                            if (range == 3)
                                words[1] = "\"" + Major.ToString() + "." + Minor.ToString() + "." + strBuild + "." + strRevision + "\"";
                            if (range == 4)
                                words[1] = "\"" + Major.ToString() + "." + Minor.ToString() + "." + strBuild + "." + strRevision + "\"";

                            line = "";

                            foreach (string word in words)
                            {
                                line += word;
                            }
                        }
                        sb.AppendLine(line);
                    }
                    sr.Close();
                    using (StreamWriter outfile = new StreamWriter(path))
                    {
                        outfile.Write(sb.ToString());
                        outfile.Close();
                    }
                }
            }
            catch (Exception e)
            {
                // Let the user know what went wrong.
                Console.WriteLine("The file could not be read:");
                Console.WriteLine(e.Message);
            }
        }
    }
}