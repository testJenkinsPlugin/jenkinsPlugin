namespace VersionChanger
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.statusStrip1 = new System.Windows.Forms.StatusStrip();
            this.toolStripStatusLabel1 = new System.Windows.Forms.ToolStripStatusLabel();
            this.label2 = new System.Windows.Forms.Label();
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.button1 = new System.Windows.Forms.Button();
            this.label3 = new System.Windows.Forms.Label();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.comboBox1 = new System.Windows.Forms.ComboBox();
            this.BackButton = new System.Windows.Forms.Button();
            this.NextButton = new System.Windows.Forms.Button();
            this.groupBox4 = new System.Windows.Forms.GroupBox();
            this.buttonSave = new System.Windows.Forms.Button();
            this.richTextBox1 = new System.Windows.Forms.RichTextBox();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.label8 = new System.Windows.Forms.Label();
            this.label7 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.pictureBox2 = new System.Windows.Forms.PictureBox();
            this.treeView1 = new System.Windows.Forms.TreeView();
            this.comboRevision = new System.Windows.Forms.ComboBox();
            this.comboBuild = new System.Windows.Forms.ComboBox();
            this.comboMinor = new System.Windows.Forms.ComboBox();
            this.comboMajor = new System.Windows.Forms.ComboBox();
            this.label5 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.textRevision = new System.Windows.Forms.TextBox();
            this.textBuild = new System.Windows.Forms.TextBox();
            this.textMinor = new System.Windows.Forms.TextBox();
            this.textMajor = new System.Windows.Forms.TextBox();
            this.statusStrip1.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.groupBox4.SuspendLayout();
            this.groupBox3.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox2)).BeginInit();
            this.SuspendLayout();
            // 
            // statusStrip1
            // 
            this.statusStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripStatusLabel1});
            this.statusStrip1.Location = new System.Drawing.Point(0, 279);
            this.statusStrip1.Name = "statusStrip1";
            this.statusStrip1.Size = new System.Drawing.Size(482, 22);
            this.statusStrip1.TabIndex = 0;
            this.statusStrip1.Text = "statusStrip1";
            // 
            // toolStripStatusLabel1
            // 
            this.toolStripStatusLabel1.Name = "toolStripStatusLabel1";
            this.toolStripStatusLabel1.Size = new System.Drawing.Size(38, 17);
            this.toolStripStatusLabel1.Text = "Ready";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(3, 27);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(48, 13);
            this.label2.TabIndex = 2;
            this.label2.Text = "Solution:";
            // 
            // textBox1
            // 
            this.textBox1.Location = new System.Drawing.Point(6, 51);
            this.textBox1.Name = "textBox1";
            this.textBox1.ReadOnly = true;
            this.textBox1.Size = new System.Drawing.Size(367, 20);
            this.textBox1.TabIndex = 3;
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(375, 49);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(75, 23);
            this.button1.TabIndex = 4;
            this.button1.Text = "Browse";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.BrowseButton_Click);
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(3, 74);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(58, 13);
            this.label3.TabIndex = 5;
            this.label3.Text = "Language:";
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.groupBox2);
            this.groupBox1.Controls.Add(this.BackButton);
            this.groupBox1.Controls.Add(this.NextButton);
            this.groupBox1.Location = new System.Drawing.Point(7, -1);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(468, 278);
            this.groupBox1.TabIndex = 6;
            this.groupBox1.TabStop = false;
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.label2);
            this.groupBox2.Controls.Add(this.textBox1);
            this.groupBox2.Controls.Add(this.button1);
            this.groupBox2.Controls.Add(this.comboBox1);
            this.groupBox2.Controls.Add(this.label3);
            this.groupBox2.Location = new System.Drawing.Point(5, 57);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(456, 131);
            this.groupBox2.TabIndex = 7;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Project View";
            // 
            // comboBox1
            // 
            this.comboBox1.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboBox1.FormattingEnabled = true;
            this.comboBox1.Items.AddRange(new object[] {
            ".NET",
            "C/C++"});
            this.comboBox1.Location = new System.Drawing.Point(6, 92);
            this.comboBox1.Name = "comboBox1";
            this.comboBox1.Size = new System.Drawing.Size(121, 21);
            this.comboBox1.TabIndex = 6;
            this.comboBox1.SelectedIndexChanged += new System.EventHandler(this.LanguageComboBox_SelectedIndexChanged);
            // 
            // BackButton
            // 
            this.BackButton.Enabled = false;
            this.BackButton.Location = new System.Drawing.Point(306, 251);
            this.BackButton.Name = "BackButton";
            this.BackButton.Size = new System.Drawing.Size(75, 23);
            this.BackButton.TabIndex = 8;
            this.BackButton.Text = "Back";
            this.BackButton.UseVisualStyleBackColor = true;
            this.BackButton.Click += new System.EventHandler(this.BackButton_Click);
            // 
            // NextButton
            // 
            this.NextButton.Location = new System.Drawing.Point(387, 251);
            this.NextButton.Name = "NextButton";
            this.NextButton.Size = new System.Drawing.Size(75, 23);
            this.NextButton.TabIndex = 7;
            this.NextButton.Text = "Next";
            this.NextButton.UseVisualStyleBackColor = true;
            this.NextButton.Click += new System.EventHandler(this.NextButton_Click);
            // 
            // groupBox4
            // 
            this.groupBox4.Controls.Add(this.buttonSave);
            this.groupBox4.Controls.Add(this.richTextBox1);
            this.groupBox4.Location = new System.Drawing.Point(13, 463);
            this.groupBox4.Name = "groupBox4";
            this.groupBox4.Size = new System.Drawing.Size(456, 162);
            this.groupBox4.TabIndex = 9;
            this.groupBox4.TabStop = false;
            this.groupBox4.Text = "Info";
            // 
            // buttonSave
            // 
            this.buttonSave.Location = new System.Drawing.Point(375, 133);
            this.buttonSave.Name = "buttonSave";
            this.buttonSave.Size = new System.Drawing.Size(75, 23);
            this.buttonSave.TabIndex = 2;
            this.buttonSave.Text = "Save";
            this.buttonSave.UseVisualStyleBackColor = true;
            this.buttonSave.Click += new System.EventHandler(this.SaveButton_Click);
            // 
            // richTextBox1
            // 
            this.richTextBox1.Location = new System.Drawing.Point(3, 19);
            this.richTextBox1.Name = "richTextBox1";
            this.richTextBox1.Size = new System.Drawing.Size(446, 108);
            this.richTextBox1.TabIndex = 1;
            this.richTextBox1.Text = "";
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add(this.label8);
            this.groupBox3.Controls.Add(this.label7);
            this.groupBox3.Controls.Add(this.label6);
            this.groupBox3.Controls.Add(this.pictureBox2);
            this.groupBox3.Controls.Add(this.treeView1);
            this.groupBox3.Controls.Add(this.comboRevision);
            this.groupBox3.Controls.Add(this.comboBuild);
            this.groupBox3.Controls.Add(this.comboMinor);
            this.groupBox3.Controls.Add(this.comboMajor);
            this.groupBox3.Controls.Add(this.label5);
            this.groupBox3.Controls.Add(this.label4);
            this.groupBox3.Controls.Add(this.textRevision);
            this.groupBox3.Controls.Add(this.textBuild);
            this.groupBox3.Controls.Add(this.textMinor);
            this.groupBox3.Controls.Add(this.textMajor);
            this.groupBox3.Location = new System.Drawing.Point(13, 295);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size(456, 162);
            this.groupBox3.TabIndex = 8;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "Properties";
            // 
            // label8
            // 
            this.label8.AutoSize = true;
            this.label8.Location = new System.Drawing.Point(208, 105);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(177, 13);
            this.label8.TabIndex = 21;
            this.label8.Text = "* - increment version\'s block number";
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(205, 82);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(184, 13);
            this.label7.TabIndex = 20;
            this.label7.Text = "# - leave version\'s block number as is";
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(26, 19);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(35, 13);
            this.label6.TabIndex = 19;
            this.label6.Text = "label6";
            // 
            // pictureBox2
            // 
            this.pictureBox2.Location = new System.Drawing.Point(6, 19);
            this.pictureBox2.Name = "pictureBox2";
            this.pictureBox2.Size = new System.Drawing.Size(16, 17);
            this.pictureBox2.TabIndex = 18;
            this.pictureBox2.TabStop = false;
            // 
            // treeView1
            // 
            this.treeView1.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.treeView1.CheckBoxes = true;
            this.treeView1.Location = new System.Drawing.Point(3, 16);
            this.treeView1.Name = "treeView1";
            this.treeView1.Size = new System.Drawing.Size(196, 143);
            this.treeView1.TabIndex = 0;
            this.treeView1.AfterCheck += new System.Windows.Forms.TreeViewEventHandler(this.TreeView_AfterCheck);
            this.treeView1.AfterSelect += new System.Windows.Forms.TreeViewEventHandler(this.TreeView_AfterSelect);
            // 
            // comboRevision
            // 
            this.comboRevision.FormattingEnabled = true;
            this.comboRevision.Items.AddRange(new object[] {
            "#",
            "*"});
            this.comboRevision.Location = new System.Drawing.Point(415, 45);
            this.comboRevision.Name = "comboRevision";
            this.comboRevision.Size = new System.Drawing.Size(34, 21);
            this.comboRevision.TabIndex = 14;
            this.comboRevision.SelectedIndexChanged += new System.EventHandler(this.comboRevision_SelectedIndexChanged);
            // 
            // comboBuild
            // 
            this.comboBuild.FormattingEnabled = true;
            this.comboBuild.Items.AddRange(new object[] {
            "#",
            "*"});
            this.comboBuild.Location = new System.Drawing.Point(375, 45);
            this.comboBuild.Name = "comboBuild";
            this.comboBuild.Size = new System.Drawing.Size(34, 21);
            this.comboBuild.TabIndex = 13;
            this.comboBuild.SelectedIndexChanged += new System.EventHandler(this.comboBuild_SelectedIndexChanged);
            // 
            // comboMinor
            // 
            this.comboMinor.FormattingEnabled = true;
            this.comboMinor.Items.AddRange(new object[] {
            "#",
            "*"});
            this.comboMinor.Location = new System.Drawing.Point(335, 45);
            this.comboMinor.Name = "comboMinor";
            this.comboMinor.Size = new System.Drawing.Size(34, 21);
            this.comboMinor.TabIndex = 12;
            this.comboMinor.SelectedIndexChanged += new System.EventHandler(this.comboMinor_SelectedIndexChanged);
            // 
            // comboMajor
            // 
            this.comboMajor.FormattingEnabled = true;
            this.comboMajor.Items.AddRange(new object[] {
            "#",
            "*"});
            this.comboMajor.Location = new System.Drawing.Point(295, 45);
            this.comboMajor.Name = "comboMajor";
            this.comboMajor.Size = new System.Drawing.Size(34, 21);
            this.comboMajor.TabIndex = 11;
            this.comboMajor.SelectedIndexChanged += new System.EventHandler(this.comboMajor_SelectedIndexChanged);
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(205, 45);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(74, 13);
            this.label5.TabIndex = 10;
            this.label5.Text = "Version Mask:";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(205, 19);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(82, 13);
            this.label4.TabIndex = 9;
            this.label4.Text = "Current Version:";
            // 
            // textRevision
            // 
            this.textRevision.Location = new System.Drawing.Point(416, 16);
            this.textRevision.Name = "textRevision";
            this.textRevision.ReadOnly = true;
            this.textRevision.Size = new System.Drawing.Size(34, 20);
            this.textRevision.TabIndex = 4;
            // 
            // textBuild
            // 
            this.textBuild.Location = new System.Drawing.Point(375, 16);
            this.textBuild.Name = "textBuild";
            this.textBuild.ReadOnly = true;
            this.textBuild.Size = new System.Drawing.Size(34, 20);
            this.textBuild.TabIndex = 3;
            // 
            // textMinor
            // 
            this.textMinor.Location = new System.Drawing.Point(335, 16);
            this.textMinor.Name = "textMinor";
            this.textMinor.ReadOnly = true;
            this.textMinor.Size = new System.Drawing.Size(34, 20);
            this.textMinor.TabIndex = 2;
            // 
            // textMajor
            // 
            this.textMajor.Location = new System.Drawing.Point(295, 16);
            this.textMajor.Name = "textMajor";
            this.textMajor.ReadOnly = true;
            this.textMajor.Size = new System.Drawing.Size(34, 20);
            this.textMajor.TabIndex = 1;
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(482, 301);
            this.Controls.Add(this.groupBox4);
            this.Controls.Add(this.groupBox3);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.statusStrip1);
            this.MaximizeBox = false;
            this.MaximumSize = new System.Drawing.Size(490, 335);
            this.MinimumSize = new System.Drawing.Size(490, 335);
            this.Name = "Form1";
            this.ShowIcon = false;
            this.Text = "Version Changer";
            this.statusStrip1.ResumeLayout(false);
            this.statusStrip1.PerformLayout();
            this.groupBox1.ResumeLayout(false);
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            this.groupBox4.ResumeLayout(false);
            this.groupBox3.ResumeLayout(false);
            this.groupBox3.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox2)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.StatusStrip statusStrip1;
        private System.Windows.Forms.ToolStripStatusLabel toolStripStatusLabel1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox textBox1;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.Button BackButton;
        private System.Windows.Forms.Button NextButton;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.GroupBox groupBox4;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.TextBox textRevision;
        private System.Windows.Forms.TextBox textBuild;
        private System.Windows.Forms.TextBox textMinor;
        private System.Windows.Forms.TextBox textMajor;
        private System.Windows.Forms.ComboBox comboRevision;
        private System.Windows.Forms.ComboBox comboBuild;
        private System.Windows.Forms.ComboBox comboMinor;
        private System.Windows.Forms.ComboBox comboMajor;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.TreeView treeView1;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.PictureBox pictureBox2;
        private System.Windows.Forms.ComboBox comboBox1;
        private System.Windows.Forms.RichTextBox richTextBox1;
        private System.Windows.Forms.Button buttonSave;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.Label label7;
    }
}

