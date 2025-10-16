import java.io.*;
import java.net.*;

public class ServerFile {
    static final int PUERTO = 5001;
    // Ruta de almacenamiento en el servidor
    static final String SERVER_FOLDER = "X:\\Imagenes\\";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());
                new Thread(new ManejadorCliente(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ManejadorCliente implements Runnable {
    private Socket socket;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream())
        ) {
            while (true) {
                String opcion = entrada.readUTF(); // Recibe opción del cliente

                if (opcion.equalsIgnoreCase("Exit")) {
                    System.out.println("Cliente desconectado.");
                    break;
                }

                switch (opcion) {
                    case "Sendfile":
                        recibirArchivo(entrada, salida);
                        break;
                    case "Getfile":
                        enviarArchivo(entrada, salida);
                        break;
                    default:
                        salida.writeUTF("Opción no válida.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error con cliente: " + e.getMessage());
        }
    }

    private void recibirArchivo(DataInputStream entrada, DataOutputStream salida) throws IOException {
        String nombreArchivo = entrada.readUTF();
        long tamano = entrada.readLong();

        File file = new File(ServerFile.SERVER_FOLDER + nombreArchivo);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            long bytesRecibidos = 0;
            int leidos;

            while (bytesRecibidos < tamano && (leidos = entrada.read(buffer)) != -1) {
                fos.write(buffer, 0, leidos);
                bytesRecibidos += leidos;
            }
        }

        salida.writeUTF("Archivo recibido correctamente en el servidor: " + nombreArchivo);
        System.out.println("Archivo recibido: " + nombreArchivo);
    }

    private void enviarArchivo(DataInputStream entrada, DataOutputStream salida) throws IOException {
        String nombreArchivo = entrada.readUTF();
        File file = new File(ServerFile.SERVER_FOLDER + nombreArchivo);

        if (!file.exists()) {
            salida.writeUTF("El archivo no existe en el servidor.");
            return;
        }

        salida.writeUTF("OK");
        salida.writeLong(file.length());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int leidos;
            while ((leidos = fis.read(buffer)) != -1) {
                salida.write(buffer, 0, leidos);
            }
        }

        System.out.println("Archivo enviado: " + nombreArchivo);
    }
}
