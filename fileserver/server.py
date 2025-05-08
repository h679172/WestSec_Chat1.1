from flask import Flask, request, jsonify, send_file, abort, Response
import os
import sqlite3
import time
from werkzeug.utils import secure_filename
import io

app = Flask(__name__)

UPLOAD_FOLDER = 'uploaded_files'
DB_FILE = 'chat_files.db'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# --- Init DB ---
def init_db():
    with sqlite3.connect(DB_FILE) as conn:
        conn.execute('''
            CREATE TABLE IF NOT EXISTS files (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                filename TEXT,
                sender TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        ''')
init_db()

# --- Delete files older than X days ---
def delete_old_files(folder, max_age_days=7):
    now = time.time()
    cutoff = now - (max_age_days * 86400)

    for filename in os.listdir(folder):
        filepath = os.path.join(folder, filename)
        if os.path.isfile(filepath) and os.path.getmtime(filepath) < cutoff:
            try:
                os.remove(filepath)
                print(f"[CLEANUP] Deleted old file: {filename}")
            except Exception as e:
                print(f"[ERROR] Cleanup failed for {filename}: {e}")

@app.before_request
def cleanup_files():
    delete_old_files(app.config['UPLOAD_FOLDER'], max_age_days=7)

# --- Upload Endpoint ---
@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files or 'sender' not in request.form:
        return jsonify({'error': 'Missing file or sender'}), 400

    file = request.files['file']
    sender = request.form['sender']
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    filename = secure_filename(file.filename)
    save_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    file.save(save_path)

    with sqlite3.connect(DB_FILE) as conn:
        conn.execute("INSERT INTO files (filename, sender) VALUES (?, ?)", (filename, sender))

    print(f"[UPLOAD] Received file: {filename} from {sender}")
    return jsonify({'message': 'File uploaded successfully', 'filename': filename})

# --- Audio Upload Endpoint ---
@app.route('/upload_voice', methods=['POST'])
def upload_voice():
    MAX_FILE_SIZE = 2 * 1024 * 1024  # 2 MB

    if 'file' not in request.files or 'sender' not in request.form:
        print("[DEBUG] Missing 'file' or 'sender'")
        return jsonify({'error': 'Missing file or sender'}), 400

    file = request.files['file']
    sender = request.form['sender']
    filename = secure_filename(file.filename)

    print(f"[DEBUG] Got file: {filename}, from: {sender}")

    # Check for correct extension
    if not filename.lower().endswith('.wav.enc'):
        print("[DEBUG] Invalid file extension")
        return jsonify({'error': 'Invalid file type'}), 400

    # Check file size
    file.seek(0, os.SEEK_END)
    file_length = file.tell()
    file.seek(0)
    print(f"[DEBUG] File size: {file_length} bytes")

    if file_length > MAX_FILE_SIZE:
        print("[DEBUG] Rejected: file too large")
        return jsonify({'error': 'File too large'}), 413

    # Save file
    save_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    file.save(save_path)

    # Store in database
    with sqlite3.connect(DB_FILE) as conn:
        conn.execute("INSERT INTO files (filename, sender) VALUES (?, ?)", (filename, sender))

    print(f"[VOICE UPLOAD] {sender} uploaded voice file: {filename}")
    return jsonify({'message': 'Voice file uploaded successfully', 'filename': filename})


# --- One-Time Download Endpoint ---
@app.route('/uploaded_files/<filename>', methods=['GET'])
def download_file_once(filename):
    file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)

    if not os.path.exists(file_path):
        return abort(404, description="File not found")

    try:
        # Load file into memory
        with open(file_path, 'rb') as f:
            file_data = f.read()

        # Delete the file
        try:
            os.remove(file_path)
            print(f"[DOWNLOAD] File served and deleted: {filename}")
            with sqlite3.connect(DB_FILE) as conn:
                conn.execute("DELETE FROM files WHERE filename = ?", (filename,))
        except Exception as e:
            print(f"[ERROR] Could not delete file {filename}: {e}")

        # Send as downloadable response
        return Response(
            io.BytesIO(file_data),
            mimetype='application/octet-stream',
            headers={'Content-Disposition': f'attachment; filename="{filename}"'}
        )

    except Exception as e:
        print(f"[ERROR] Failed to serve file {filename}: {e}")
        return abort(500, description="Server error while downloading file.")

# --- Optional: List files (for debug/admin use) ---
@app.route('/files', methods=['GET'])
def list_files():
    with sqlite3.connect(DB_FILE) as conn:
        cursor = conn.execute("SELECT filename, sender, timestamp FROM files ORDER BY id DESC")
        return jsonify(cursor.fetchall())

# --- Run the app ---
if __name__ == '__main__':
    app.run(host='91.189.120.116', port=5000)
