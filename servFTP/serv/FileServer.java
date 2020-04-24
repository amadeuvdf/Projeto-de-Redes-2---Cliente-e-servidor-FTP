import java.net.*;
import java.io.*;

public class FileServer {
	Socket socket;

	public FileServer() {
		try {
			ServerSocket ss = new ServerSocket(9191);
			while (true) {
				System.out.println("Servidor on");
				socket = ss.accept();
				System.out.println("Cliente conectado!");
				Login();
				leComando();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new FileServer();
	}

	public void RecebeArquivo() {
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			String fileName = in.readUTF();
			long size = in.readLong();

			System.out.println("Processando arquivo: " + fileName + " - " + size + " bytes.");

			FileOutputStream fos = new FileOutputStream(fileName);
			byte[] buf = new byte[6096];
			while (true) {
				int len = in.read(buf);
				if (len == -1)
					break;
				fos.write(buf, 0, len);
			}
			fos.flush();
			fos.close();
			System.out.println("Pronto.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void MandaArquivo(String nomeArquivo) {
		File f = new File(nomeArquivo);

		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
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
		String retorno = "Erro na execução do comando";
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			// -- Windows --
			Process process = Runtime.getRuntime().exec("cmd /c " + comando);

			retorno = "";

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				retorno += line + "\n";
			}
			out.writeUTF(retorno);
			out.close();
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
				out.writeUTF("verdadeiro");
				out.writeUTF("Entre com seu usuario:");
				String usuario = in.readUTF();
				out.writeUTF("Entre com sua senha:");
				String senha = in.readUTF();
				if (usuario.equals("usuario") && senha.equals("senha")) {
					login_errado = false;
					out.writeUTF("falso");
					// out.flush();
					// in.close();
				}
				out.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void leComando() {
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			String msg = in.readUTF();
			if (!msg.equals("!")) {
				String campos[] = msg.split(" ");

				if (campos[0].equals("get") && campos.length > 1) {
					MandaArquivo(campos[1]);
				} else if (campos[0].equals("put") && campos.length > 1) {
					RecebeArquivo();
				} else {
					RodaComando(campos[0]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}