namespace Graficos
{
    using System;
    using System.Collections.Generic;
    using System.IO;
    using System.Windows.Forms;
    using ScottPlot;

    class CruzamentoRetasGeradorLuizHenrique
    {   
        [STAThread]
        public static void Gerar()
        {
            int numeroDeRetas = 6;
            Random random = new Random();
            string filePath = "C:\\Users\\laboratorio\\source\\repos\\Graficos\\Graficos\\Retas.txt";

            List<(int, int, int, int)> retas = new List<(int, int, int, int)>();


            for (int i = 0; i < numeroDeRetas; i++)
            {
                int x1 = random.Next(-10, 10);
                int y1 = random.Next(-10, 10);
                int x2 = random.Next(-10, 10);
                int y2 = random.Next(-10, 10);

                retas.Add((x1, y1, x2, y2));
            }


            try
            {
                using (StreamWriter writer = new StreamWriter(filePath))
                {
                    foreach (var reta in retas)
                    {

                        writer.WriteLine($"({reta.Item1},{reta.Item2});({reta.Item3},{reta.Item4})");
                    }
                }
                Console.WriteLine("Retas geradas e salvas com sucesso no arquivo.");
            }
            catch (Exception ex)
            {
                Console.WriteLine("Erro ao salvar o arquivo: " + ex.Message);
            }
        }
        static void Main()
        {
            Gerar();
            string filePath = ("C:\\Users\\laboratorio\\source\\repos\\Graficos\\Graficos\\Retas.txt");

            List<(int, int, int, int)> retas = new List<(int, int, int, int)>();

            try
            {
                foreach (string line in File.ReadAllLines(filePath))
                {
                    var pontos = line.Split(';');
                    var p1 = pontos[0].Replace("(", "").Replace(")", "").Split(',');
                    var p2 = pontos[1].Replace("(", "").Replace(")", "").Split(',');

                    int x1 = int.Parse(p1[0]);
                    int y1 = int.Parse(p1[1]);
                    int x2 = int.Parse(p2[0]);
                    int y2 = int.Parse(p2[1]);

                    retas.Add((x1, y1, x2, y2));
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Erro ao ler o arquivo: " + ex.Message);
                return;
            }

            List<(double, double)> intersecoes = new List<(double, double)>();
            List<string> retasParalelas = new List<string>();  // Lista para armazenar retas paralelas

            for (int i = 0; i < retas.Count; i++)
            {
                for (int j = i + 1; j < retas.Count; j++)
                {
                    var (x1, y1, x2, y2) = retas[i];
                    var (x3, y3, x4, y4) = retas[j];

                    double m1 = (double)(y2 - y1) / (x2 - x1);
                    double b1 = y2 - m1 * x2;
                    double m2 = (double)(y4 - y3) / (x4 - x3);
                    double b2 = y3 - m2 * x3;

                    if (m1 == m2)  // Se as retas s�o paralelas
                    {
                        retasParalelas.Add($"Reta {i + 1} e Reta {j + 1} s�o paralelas");
                    }
                    else
                    {
                        double xInt = (b2 - b1) / (m1 - m2);
                        double yInt = m1 * xInt + b1;
                        intersecoes.Add((xInt, yInt));
                    }
                }
            }

            
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Form form = new Form() { Width = 800, Height = 600, Text = "Interse��o de Retas" };
            ScottPlot.WinForms.FormsPlot formsPlot = new() { Dock = DockStyle.Fill };
            form.Controls.Add(formsPlot);

            
            System.Windows.Forms.Label labelMensagens = new System.Windows.Forms.Label()
            {
                Dock = DockStyle.Top,
                AutoSize = true,
                Padding = new Padding(10)
            };
            form.Controls.Add(labelMensagens);

            
            if (retasParalelas.Count > 0)
            {
                string mensagens = "As seguintes retas s�o paralelas:\n";
                foreach (var msg in retasParalelas)
                {
                    mensagens += msg + "\n";
                }
                labelMensagens.Text = mensagens;
            }
            else
            {
                labelMensagens.Text = "N�o h� retas paralelas.";
            }

           
            var plt = formsPlot.Plot;
            plt.Axes.SetLimits(-10, 10, -10, 10);
            plt.Title("Interse��o de Retas");
            plt.Axes.Bottom.Label.Text = "X";
            plt.Axes.Left.Label.Text = "Y";

            foreach (var (x1, y1, x2, y2) in retas)
            {
                double fator = 10;

                double x1_ext = x1 - fator * (x2 - x1);
                double y1_ext = y1 - fator * (y2 - y1);
                double x2_ext = x2 + fator * (x2 - x1);
                double y2_ext = y2 + fator * (y2 - y1);

                plt.Add.Line(x1_ext, y1_ext, x2_ext, y2_ext);
            }

            foreach (var (x, y) in intersecoes)
            {
                plt.Add.ScatterPoints(new double[] { x }, new double[] { y }).Color = ScottPlot.Colors.Black;
            }

            formsPlot.Refresh();
            Application.Run(form);
        }
    }
}
