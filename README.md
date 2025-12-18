# 📊 HƯỚNG DẪN LẤY ĐIỂM & SỬ DỤNG PHẦN MỀM TÍNH GPA

## 1. Mục đích
Tài liệu này hướng dẫn:
- Lấy dữ liệu điểm từ trang web bằng JavaScript
- Chuẩn hoá dữ liệu (xoá HTML, ký tự đặc biệt)
- Xuất dữ liệu ra file `diem.json`
- Sử dụng file `diem.json` cho phần mềm tính GPA (Java Swing)

---

## 2. Cách lấy điểm từ website (JavaScript)

### Bước 1: Mở trang web bảng điểm
- Truy cập trang web có bảng điểm (HTML `<table>`)
- Đảm bảo bảng điểm đã tải đầy đủ

---

### Bước 2: Mở Developer Tools
- Nhấn **F12**
- Chọn tab **Console**

---

### Bước 3: Dán đoạn mã JavaScript sau vào Console
- Đoạn này bú của ông nào đấy
- Dán xong tải về chạy app bỏ vào thôi

```javascript
function decodeHtmlEntities(text) {
    const entities = [
        ['amp', '&'],
        ['apos', "'"],
        ['lt', '<'],
        ['gt', '>'],
        ['quot', '"'],
    ];
    for (let i = 0; i < entities.length; i++) {
        text = text.replace(
            new RegExp(`&${entities[i][0]};`, 'g'),
            entities[i][1]
        );
    }
    return text;
}

let table = document.getElementsByTagName('table');
let tableScore = table[1];
let elementScores = tableScore.getElementsByClassName('pointer');
let scoreAll = [];

for (let tr of elementScores) {
    let score = {};
    let tdList = tr.getElementsByTagName('td');

    score.id = tdList[0] ? tdList[0].innerHTML : '';
    if (score.id !== '') score.id = parseInt(score.id);

    let nameField = tdList[1] ? tdList[1].innerHTML : '';
    score.name = decodeHtmlEntities(
        nameField.replace(/<[^>]+>/g, '').replace('!!', '')
    ).trim();

    if (score.name === '') continue;

    score.countTC = tdList[2] ? parseInt(tdList[2].innerHTML) : '';
    score.countLH = tdList[3] ? parseInt(tdList[3].innerHTML) : '';
    score.scoreCC = tdList[4] ? parseFloat(tdList[4].innerHTML) : '';
    score.scoreBT = tdList[5] ? parseFloat(tdList[5].innerHTML) : '';
    score.scoreGK = tdList[6] ? parseFloat(tdList[6].innerHTML) : '';
    score.scoreCK = tdList[7] ? parseFloat(tdList[7].innerHTML) : '';

    let scoreT10Field = tdList[8] ? tdList[8].innerHTML : '';
    let scoreT10Match = scoreT10Field.match(/<b>(.*?)<\/b>/);
    score.scoreT10 = scoreT10Match ? parseFloat(scoreT10Match[1]) : '';

    let scoreChField = tdList[9] ? tdList[9].innerHTML : '';
    let scoreChMatch = scoreChField.match(/<b[^>]*>(.*?)<\/b>/);
    score.scoreCh = scoreChMatch ? scoreChMatch[1] : '';

    scoreAll.push(score);
}

// Loại bỏ môn trùng, giữ điểm cao nhất
let duplicate = {};
scoreAll.forEach(score => {
    if (!duplicate[score.name] ||
        score.scoreT10 > duplicate[score.name].scoreT10) {
        duplicate[score.name] = score;
    }
});
scoreAll = Object.values(duplicate);

// Xuất file JSON
let dataDownload = { scoreAll };
let json = JSON.stringify(dataDownload);
const blob = new Blob([json], { type: 'application/json' });
const url = URL.createObjectURL(blob);

const link = document.createElement('a');
link.href = url;
link.download = 'diem.json';
link.click();

URL.revokeObjectURL(url);
link.remove();
