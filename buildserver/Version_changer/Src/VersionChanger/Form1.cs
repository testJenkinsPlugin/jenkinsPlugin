using System;
using System.Reflection;
using System.IO;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Text.RegularExpressions;

namespace VersionChanger
{
    public partial class Form1 : Form
    {
        #region Local variables
        Schema schema;
        TreeNode Solution;
        TreeNode Project;
        //TreeNode Info;
        TreeNode SelectedItem = null;
        bool comboIsModify = false;
        string ProjectPath="";
        int PageIndex=1;
        #endregion

        public Form1()
        {
            InitializeComponent();
            Assembly thisAssembly = System.Reflection.Assembly.GetAssembly(this.GetType());
            schema = new Schema();
            SecondPage(false);
            ThirdPage(false);
            treeView1.SelectedImageIndex = -1;
            comboBox1.SelectedIndex = 0;
            groupBox2.Location = new Point(6, 83);
            groupBox3.Location = new Point (6,83);
            groupBox4.Location = new Point (6,83);
            this.Size = new Size(490, 335);
        }

        #region Pages functions
        private void BackButton_Click(object sender, EventArgs e)
        {
            PageIndex--;
            SwitchPages();
        }
        private void NextButton_Click(object sender, EventArgs e)
        {
            PageIndex++;
            SwitchPages();
        }
        private void SwitchPages()
        {
            if (PageIndex == 1)
            {
                FirstPage(true);
                SecondPage(false);
                ThirdPage(false);
                BackButton.Enabled = false;
            }
            else if (PageIndex == 2)
            {
                if (textBox1.Text.Length > 0)
                {
                    FirstPage(false);
                    SecondPage(true);
                    ThirdPage(false);
                    BackButton.Enabled = true;
                    NextButton.Enabled = true;
                }
                else
                {
                    PageIndex--;
                    SwitchPages();
                }
            }
            else if (PageIndex == 3)
            {
                FirstPage(false);
                SecondPage(false);
                ThirdPage(true);
                NextButton.Enabled = false;
            }
        }
        private void FirstPage(bool state)
        {
            groupBox2.Visible = state;
        }
        private void SecondPage(bool state)
        {
            groupBox3.Visible = state;
            if (comboBox1.SelectedIndex == 0 && state)
            {      
                if (treeView1.Nodes.Count == 0 || ProjectPath =="")
                {
                    ProjectPath = textBox1.Text;
                    treeView1.BeginUpdate();
                    treeView1.Nodes.Clear();
                    schema.Files.Clear();
                    Solution = new TreeNode(Path.GetFileNameWithoutExtension(textBox1.Text));
                    treeView1.Nodes.Add(Solution);
                    Solution.ImageIndex = -1;
                    treeView1.EndUpdate();
                    DotNETProjectSearcher(textBox1.Text, "csproj");
                    DotNETProjectSearcher(textBox1.Text, "vbproj");
                    DotNETProjectSearcher(textBox1.Text, "vjsproj");
                }

                pictureBox2.Visible = false;
                label6.Visible = false;
                treeView1.Visible = true;
            }
            else if (comboBox1.SelectedIndex == 1 && state)
            {
                ProjectPath = textBox1.Text;                
                label6.Text = Path.GetFileName(textBox1.Text);
                CPlusPlusGetVersion(textBox1.Text);

                pictureBox2.Visible = true;
                label6.Visible = true;
                treeView1.Visible = false;
            }
        }
        private void ThirdPage(bool state)
        {
            groupBox4.Visible = state;
            if (PageIndex == 3)
            {
                richTextBox1.Text = "Click \"Save\" button to generate the configuration .exe-file.\n\nNote! Executing of the .exe-file will modify the following list of files:\n-------------------\n";
                if (comboBox1.SelectedIndex == 0)
                    foreach (Data d in schema.Files)
                        richTextBox1.Text += "File to modify: " + d.Parent + "\t with mask: " + d.Major + " " + d.Minor + " " + d.Build + " " + d.Revision + "\n";

                else if (comboBox1.SelectedIndex == 1)
                    richTextBox1.Text += "File to modify:" + textBox1.Text + "\t with mask: " + comboMajor.SelectedItem + " " + comboMinor.SelectedItem + " " + comboBuild.SelectedItem + " " + comboRevision.SelectedItem + "\n";

            }
        }
        #endregion

        #region Functions
        private void DotNETProjectSearcher(string path,string pattern)
        {
            try
            {
                using (StreamReader sr = new StreamReader(path))
                {
                    String line;
                    while ((line = sr.ReadLine()) != null)
                    {
                        if (line.Contains(pattern))
                        {
                            char[] delimiterChars = { '"' };
                            string[] words = line.Split(delimiterChars);
                            foreach (string word in words)
                            {
                                if (word.Contains(pattern))
                                {
                                    if (pattern == "csproj")
                                    {
                                        Project = new TreeNode(Path.GetFileName(path + "\\" + word));
                                        Project.ImageIndex = 1;
                                        Project.SelectedImageIndex = 1;
                                        Solution.Nodes.Add(Project);
                                        DotNETProjectSearcher(Path.GetDirectoryName(path) + "\\" + word, "AssemblyInfo.cs");
                                    }
                                    if (pattern == "vbproj")
                                    {
                                        Project = new TreeNode(Path.GetFileName(path + "\\" + word));
                                        Project.ImageIndex = 2;
                                        Project.SelectedImageIndex = 2;
                                        Solution.Nodes.Add(Project);
                                        DotNETProjectSearcher(Path.GetDirectoryName(path) + "\\" + word, "AssemblyInfo.vb");
                                    }
                                    if (pattern == "vjsproj")
                                    {
                                        Project = new TreeNode(Path.GetFileName(path + "\\" + word));
                                        Project.ImageIndex = 3;
                                        Project.SelectedImageIndex = 3;
                                        Solution.Nodes.Add(Project);
                                        DotNETProjectSearcher(Path.GetDirectoryName(path) + "\\" + word, "AssemblyInfo.jsl");
                                    }


                                    if (pattern == "AssemblyInfo.cs" || pattern == "AssemblyInfo.vb" || pattern == "AssemblyInfo.jsl")
                                    {
                                        string[] tag = new string[5];
                                        tag[0] = Path.GetDirectoryName(path) + "\\" + word;
                                        tag[1]=tag[2]=tag[3]=tag[4]="#";
                                        Project.Tag = tag;
                                    }
                                    /////////////////////////////////////////////////
                                }
                            }
                        }
                    }
                    sr.Close();
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("The file could not be read:");
                Console.WriteLine(e.Message);
            }
        }
        private void DotNETGetVersion(object Tag)
        {
            string path = ((string[])Tag)[0];
            try
            {
                using (StreamReader sr = new StreamReader(path))
                {
                    String line;
                    StringBuilder sb = new StringBuilder();
                    //  [assembly: AssemblyVersion("1.0.0.0")]
                    //  <Assembly: AssemblyVersion("2.0.0.0")> 
                    //  /** @assembly AssemblyVersion("3.0.0.0") */
                    while ((line = sr.ReadLine()) != null)
                    {
                        //Regex.IsMatch(line ,"\\s*[[]\\s*assembly\\s*:\\sAssemblyVersion\\s*[(]\\s*[\"]\\w[.]\\w[.]\\w[.]\\w[\"]\\s*[)]\\s*[]]\\s*");
                        string pattern = null;
                        if (Path.GetExtension(path).ToUpper() == ".CS")
                            pattern = "\\s*[[]\\s*assembly\\s*:\\s*AssemblyVersion\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[]]\\s*";
                        if (Path.GetExtension(path).ToUpper() == ".VB")
                            pattern = "\\s*[<]\\s*Assembly\\s*:\\s*AssemblyVersion\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[>]\\s*";
                        if (Path.GetExtension(path).ToUpper() == ".JSL")
                            pattern = "\\s*[.*]\\s*@assembly\\s*AssemblyVersion\\s*[(]\\s*[\"]\\w*([.*]\\w*){1,}[\"]\\s*[)]\\s*[.*]\\s*";
                        if (Regex.IsMatch(line,pattern))
                        {
                            char[] delimiterChars = { '"' };
                            string[] words = line.Split(delimiterChars);

                            FillVersionFields(words[1], 0, textMajor ,comboMajor);
                            FillVersionFields(words[1], 1, textMinor ,comboMinor);
                            FillVersionFields(words[1], 2, textBuild ,comboBuild);
                            FillVersionFields(words[1], 3, textRevision, comboRevision);

                        }
                    }
                    sr.Close();
                }
                foreach (Data d in schema.Files)
                {
                    if (((string[])Tag)[0] == d.Path)
                    {
                        if (d.Major.ToString() == "#")
                            comboMajor.SelectedIndex = 0;
                        else if (d.Major.ToString() == "*")
                            comboMajor.SelectedIndex = 1;

                        if (d.Minor.ToString() == "#")
                            comboMinor.SelectedIndex = 0;
                        else if (d.Minor.ToString() == "*")
                            comboMinor.SelectedIndex = 1;

                        if (d.Build.ToString() == "#")
                            comboBuild.SelectedIndex = 0;
                        else if (d.Build.ToString() == "*")
                            comboBuild.SelectedIndex = 1;

                        if (d.Revision.ToString() == "#")
                            comboRevision.SelectedIndex = 0;
                        else if (d.Revision.ToString() == "*")
                            comboRevision.SelectedIndex = 1;
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("The file could not be read:");
                Console.WriteLine(e.Message);
            }
        }
        private void CPlusPlusGetVersion(string path)
        {
            try
            {
                using (StreamReader sr = new StreamReader(path))
                {
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = sr.ReadLine()) != null)
                    {
                        string pattern = null;
                        if (Path.GetExtension(path).ToUpper() == ".H")
                            pattern = @"\s*#define\s*\w*\s*\d*.\d*.\d*.\d*\s*";
                        if (Regex.IsMatch(line, pattern))
                        {
                            char[] delimiterChars1 = { ' ' };
                            string[] words = line.Split(delimiterChars1);

                            foreach (string word in words)
                            {
                                if (word.Contains(",") && !word.Contains("\""))
                                {
                                    FillVersionFields(word, 0, textMajor, comboMajor);
                                    FillVersionFields(word, 1, textMinor, comboMinor);
                                    FillVersionFields(word, 2, textBuild, comboBuild);
                                    FillVersionFields(word, 3, textRevision, comboRevision);
                                }
                            }
                        }
                    }
                    sr.Close();
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("The file could not be read:");
                Console.WriteLine(e.Message);
            }
        }
        private void FillVersionFields(string str,int index, TextBox txt, ComboBox combo)
        {
            char[] delimiterChars2 = { '.',',' };
            string[] words = str.Split(delimiterChars2);
            try
            {
                txt.Text = words[index];
                try
                {
                    Convert.ToInt32(words[index]);
                    combo.SelectedIndex = 0;
                    combo.Enabled = true;
                }
                catch
                {
                    txt.Text = "";
                    combo.SelectedIndex = 0;
                    combo.Enabled = false;
                }
            }
            catch
            {
                txt.Text = "";
                combo.SelectedIndex = 0;
                combo.Enabled = false;
            }
        }
        #endregion


        #region Events
        private void BrowseButton_Click(object sender, EventArgs e)
        {
            OpenFileDialog dlg = new OpenFileDialog();
            if (comboBox1.SelectedIndex == 0)
                dlg.Filter = "Solution (*.sln)|*.sln";
            else if (comboBox1.SelectedIndex == 1)
                dlg.Filter = "Solution (*.h)|*.h";
            if (dlg.ShowDialog() == DialogResult.OK)
            {
                textBox1.Text = dlg.FileName;
                ProjectPath = "";
            }
        }
        private void TreeView_AfterSelect(object sender, TreeViewEventArgs e)
        {
            comboIsModify = true;
            if (treeView1.SelectedNode != null && treeView1.SelectedNode.Tag != null)
            {                
                SelectedItem = treeView1.SelectedNode;
                DotNETGetVersion(treeView1.SelectedNode.Tag);
            }
            else if (e.Node.Parent == null)
            {
                comboMajor.SelectedIndex =-1;
                comboMinor.SelectedIndex = -1;
                comboBuild.SelectedIndex = -1;
                comboRevision.SelectedIndex = -1;
                textMajor.Text ="";
                textMinor.Text = "";
                textBuild.Text = "";
                textRevision.Text = "";
            }
            comboIsModify = false;
            
        }
        private void LanguageComboBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            textBox1.Text = "";
            if(comboBox1 .SelectedIndex ==0)
                label2.Text = "Solution:";
            else if(comboBox1 .SelectedIndex ==1)
                label2.Text = "Header:";
        }
        private void SaveButton_Click(object sender, EventArgs e)
        {
            SaveFileDialog dlg = new SaveFileDialog();
            dlg.Filter = "(*.bat)|*.bat";
            if (dlg.ShowDialog() == DialogResult.OK)
            {
                StreamWriter fout = new StreamWriter(dlg.FileName);
                string exepath = Application.ExecutablePath;
                exepath = exepath.Replace(":", ":\"");
                exepath += "\"";
                fout.WriteLine("start " + exepath + " \"%~f0\"");
                fout.WriteLine("goto start");
                if (comboBox1.SelectedIndex == 0)
                {

                    foreach (Data d in schema.Files)
                    {
                        fout.WriteLine("FileToChange: " + "\"" + d.Path + "\"" + d.Major + " " + d.Minor + " " + d.Build + " " + d.Revision);
                    }
                }
                else if (comboBox1.SelectedIndex == 1)
                {
                    fout.WriteLine("FileToChange: " + "\"" + textBox1.Text + "\"" + comboMajor.SelectedItem + " " + comboMinor.SelectedItem + " " + comboBuild.SelectedItem + " " + comboRevision.SelectedItem);
                }
                fout.WriteLine(":start");
                fout.Close();
            }
        }
        private void TreeView_AfterCheck(object sender, TreeViewEventArgs e)
        {
            treeView1.SelectedNode  = e.Node;            
            if (treeView1.SelectedNode.Checked)
            {
                if (e.Node.Parent == null)
                {
                    foreach (TreeNode node in e.Node.Nodes)
                    {
                        node.Checked = true;
                    }
                }
                if (treeView1.SelectedNode != null && treeView1.SelectedNode.Tag != null)
                {
                    string[] tag = new string[6];
                    try
                    {
                        tag[0] = ((string[])treeView1.SelectedNode.Tag)[0];
                        tag[1] = comboMajor.SelectedItem.ToString();
                        tag[2] = comboMinor.SelectedItem.ToString();
                        tag[3] = comboBuild.SelectedItem.ToString();
                        tag[4] = comboRevision.SelectedItem.ToString();
                        TreeNode Parents = treeView1.SelectedNode;
                        string parent = Parents.Text;
                        while (Parents.Parent.Parent != null)
                        {
                            parent = Parents.Parent.Text + "\\" + parent;
                            Parents = Parents.Parent;
                        }
                        tag[5] = parent;
                    }
                    catch
                    { }
                    Data dat = new Data(tag);
                    foreach (Data d in schema.Files)
                    {
                        if (dat.Path == d.Path)
                        {
                            schema.Files.Remove(d);
                            break;
                        }
                    }
                    schema.Add(dat);
                }
            }
            else
            {
                if (e.Node.Parent == null)
                {
                    foreach (TreeNode node in e.Node.Nodes)
                    {
                        node.Checked = false;
                    }
                }
                if (e.Node.Parent != null && e.Node.Parent.Checked)
                {
                    e.Node.Parent.Checked=false;
                }
                foreach (Data d in schema.Files)
                {
                    if (treeView1.SelectedNode.Tag!= null &&((string[])treeView1.SelectedNode.Tag)[0] == d.Path)
                    {
                        schema.Files.Remove(d);
                        break;
                    }
                }
            }
        }

        private void comboMajor_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (treeView1.SelectedNode != null && treeView1.SelectedNode.Tag != null)
                ChangeMask(treeView1.SelectedNode.Tag, 1, sender);
        }
        private void comboMinor_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (treeView1.SelectedNode != null && treeView1.SelectedNode.Tag != null)
                ChangeMask(treeView1.SelectedNode.Tag, 2, sender);
        }
        private void comboBuild_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (treeView1.SelectedNode != null && treeView1.SelectedNode.Tag != null)
                ChangeMask(treeView1.SelectedNode.Tag, 3, sender);
        }
        private void comboRevision_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (treeView1.SelectedNode != null && treeView1.SelectedNode.Tag != null)
                ChangeMask(treeView1.SelectedNode.Tag, 4, sender);
        }
        private void ChangeMask(object tag, int index, object sender)
        {
            if (comboIsModify == false && comboBox1 .SelectedIndex  == 0)
            {
                foreach (Data d in schema.Files)
                {
                    if (((string[])treeView1.SelectedNode.Tag)[0] == d.Path)
                    {
                        ((string[])tag)[index] = ((ComboBox)sender).SelectedItem.ToString();
                        if (index == 1)
                            d.Major = ((ComboBox)sender).SelectedItem.ToString();
                        if (index == 2)
                            d.Minor = ((ComboBox)sender).SelectedItem.ToString();
                        if (index == 3)
                            d.Build = ((ComboBox)sender).SelectedItem.ToString();
                        if (index == 4)
                            d.Revision = ((ComboBox)sender).SelectedItem.ToString();
                        break;
                    }
                }
            }
        }
        #endregion





        
    }
}