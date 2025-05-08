package no.westsec.chat;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileHandler {
    public static void uploadFile(String targetUrl, File file, String senderName) throws IOException {
        String boundary = Long.toHexString(System.currentTimeMillis());
        String CRLF = "\r\n";

        @SuppressWarnings("deprecation")
		HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (
            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)
        ) {
            // Send sender field
            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"sender\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=UTF-8").append(CRLF);
            writer.append(CRLF).append(senderName).append(CRLF).flush();

            // Send file
            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append(CRLF);
            writer.append("Content-Type: " + HttpURLConnection.guessContentTypeFromName(file.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();

            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            for (int length = 0; (length = inputStream.read(buffer)) > 0;) {
                output.write(buffer, 0, length);
            }
            output.flush();
            inputStream.close();

            writer.append(CRLF).flush();
            writer.append("--").append(boundary).append("--").append(CRLF).flush();
        }

        // Handle response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            System.out.println("Server Response:");
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
        } else {
            System.err.println("Upload failed. HTTP error code: " + responseCode);
        }
    }
    public static void downloadFile(String fileUrl, String savePath) throws IOException {
        try (@SuppressWarnings("deprecation")
		BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(savePath)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
    }
}
