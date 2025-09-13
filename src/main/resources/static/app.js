const fileInput = document.getElementById("videoFile");
const fileNameSpan = document.getElementById("fileName");
const uploadBox = document.getElementById("uploadBox");
const progressBox = document.getElementById("progressBox");
const progressBar = document.getElementById("progressBar");
const progressText = document.getElementById("progressText");
const resultBox = document.getElementById("resultBox");
const originalSizeSpan = document.getElementById("originalSize");
const compressedSizeSpan = document.getElementById("compressedSize");
const reductionPercentSpan = document.getElementById("reductionPercent");
const downloadBtn = document.getElementById("downloadBtn");

let compressedBlob = null;

// File select / drag-drop
fileInput.addEventListener("change", () => {
    if(fileInput.files.length > 0) {
        fileNameSpan.innerText = fileInput.files[0].name;
        resultBox.style.display = "none";
        downloadBtn.style.display = "none";
    } else {
        fileNameSpan.innerText = "📂 Click or Drag & Drop Video";
    }
});

// Drag & drop visual feedback
uploadBox.addEventListener("dragover", (e) => {
    e.preventDefault();
    uploadBox.style.borderColor = "#6c63ff";
    uploadBox.style.background = "#f0f0ff";
});

uploadBox.addEventListener("dragleave", () => {
    uploadBox.style.borderColor = "#aaa";
    uploadBox.style.background = "#fff";
});

uploadBox.addEventListener("drop", (e) => {
    e.preventDefault();
    uploadBox.style.borderColor = "#aaa";
    uploadBox.style.background = "#fff";

    const files = e.dataTransfer.files;
    if(files.length > 0) {
        fileInput.files = files;
        fileNameSpan.innerText = files[0].name;
        resultBox.style.display = "none";
        downloadBtn.style.display = "none";
    }
});

function uploadVideo() {
    if(!fileInput.files.length) {
        alert("Please select a video!");
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append("file", file);

    const xhr = new XMLHttpRequest();
    xhr.open("POST", "http://localhost:8080/compress", true);

    xhr.upload.onprogress = function(event) {
        if(event.lengthComputable) {
            let percent = Math.round((event.loaded / event.total) * 100);
            progressBox.style.display = "block";
            progressBar.value = percent;
            progressText.innerText = percent + "%";
        }
    };

    xhr.onload = function() {
        if(xhr.status === 200) {
            compressedBlob = new Blob([xhr.response], { type: "video/mp4" });
            let url = window.URL.createObjectURL(compressedBlob);

            let originalSize = (file.size / (1024*1024)).toFixed(1);
            let compressedSize = (compressedBlob.size / (1024*1024)).toFixed(1);
            let reduction = (((originalSize - compressedSize) / originalSize) * 100).toFixed(0);

            resultBox.style.display = "flex";
            originalSizeSpan.innerText = originalSize;
            compressedSizeSpan.innerText = compressedSize;
            reductionPercentSpan.innerText = "-" + reduction + "%";

            downloadBtn.style.display = "inline-block";
            downloadBtn.onclick = () => {
                let a = document.createElement("a");
                a.href = url;
                a.download = "compressed.mp4";
                a.click();
            };
        } else {
            alert("Compression failed!");
        }
    };

    xhr.responseType = "arraybuffer";
    xhr.send(formData);
}
