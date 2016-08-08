using System;
using System.Collections.Generic;
using System.Text;

namespace VersionChanger
{
    public class Data
    {
        public Data(string[] Params)
        {
            Path = Params[0];
            Major = Params[1];
            Minor = Params[2];
            Build = Params[3];
            Revision = Params[4];
            Parent = Params[5];
        }
        public string Parent;
        public string Path;
        public string Major;
        public string Minor;
        public string Build;
        public string Revision;
    }
    public class Schema
    {        
        public List<Data> Files = new List<Data>();
        public void Add(Data dat)
        {
            Files.Add(dat);
        }
    }
}
