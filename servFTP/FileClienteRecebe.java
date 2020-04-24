import java.net.*;
import java.io.*;

public class FileClienteRecebe {
    Socket socket;
    DataInputStream input = new DataInputStream(System.in);
    public String usuario = "";
    public String senha = "";

    public FileClienteRecebe() {
        try {
            System.out.println("Digite o ip do servidor a se conectar:");
            String ip = input.readLine();
            System.out.println("Digite a porta do servidor a se conectar:");
            int porta = Integer.parseInt(input.readLine());
            boolean logou = false;
            do {
                socket = new Socket(ip, porta);
                if (!logou)
                    Login();
                else {
                    LoginLogado();
                }

                logou = true;

                String escolha = "escolha";
                System.out.println("Digite o comando:");
                escolha = input.readLine();

                if (escolha.equals("quit"))
                    break;

                String campos[] = escolha.split(" ");

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(escolha);
                if (escolha.equals("!")) {
                    System.out.println("Digite o comando a ser executado em sua maquina:");
                    RodaComando(input.readLine());
                } else {

                    if (campos[0].equals("get") && campos.length > 1) {
                        RecebeArquivo();
                    } else if (campos[0].equals("put") && campos.length > 1) {
                        MandaArquivo(campos[1]);
                    } else {
                        DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        System.out.println(in.readUTF());
                    }
                }
            } while (logou);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new FileClienteRecebe();
    }

    public void RecebeArquivo() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String fileName = in.readUTF();
            File f = new File(fileName);
            long size = in.readLong();

            System.out.println("Processando arquivo: " + fileName + " - " + size + " bytes.");

            FileOutputStream fos = new FileOutputStream(f);
            byte[] buf = new byte[6096];
            while (true) {
                int len = in.read(buf);
                if (len == -1)
                    break;
                fos.write(buf, 0, len);
            }
            fos.flush();

            System.out.println("Pronto.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void MandaArquivo(String nomeArquivo) {
        File f = new File(nomeArquivo);

        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Transferindo o arquivo: " + f.getName());
            long x = f.length();
            System.out.println(x);
            out.writeUTF(f.getName());
            out.writeLong(f.length());
            FileInputStream in = new FileInputStream(f);
            byte[] buf = new byte[6096];

            while (true) {
                int len = in.read(buf);
                if (len == -1)
                    break;
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            System.out.println("Pronto.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RodaComando(String comando) {
        try {
            // -- Windows --
            Process process = Runtime.getRuntime().exec("cmd /c " + comando);

            String output = "";

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output += line + "\n";
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println(output);
            } else {
                System.out.println("Algo de errado não está certo!");
                // abnormal...
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Login() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            boolean login_errado = true;
            while (login_errado) {
                login_errado = in.readUTF().equals("verdadeiro");
                if (!login_errado)
                    break;
                System.out.println(in.readUTF());
                usuario = input.readLine();
                out.writeUTF(usuario);
                System.out.println(in.readUTF());
                senha = input.readLine();
                out.writeUTF(senha);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void LoginLogado() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            boolean login_errado = true;
            String pegavalor = "";
            login_errado = in.readUTF().equals("verdadeiro");
            pegavalor = in.readUTF();
            out.writeUTF(this.usuario);
            pegavalor = in.readUTF();
            out.writeUTF(this.senha);
            login_errado = in.readUTF().equals("verdadeiro");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}