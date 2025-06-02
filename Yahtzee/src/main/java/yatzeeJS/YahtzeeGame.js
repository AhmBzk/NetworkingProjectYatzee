// JavaScript conversion of Java Swing-based Yahtzee GUI - WebSocket connected

class YahtzeeGame {
  constructor(socket, playerName, opponentName) {
    this.socket = socket;
    this.playerName = playerName;
    this.opponentName = opponentName;

    this.diceValues = [0, 0, 0, 0, 0];
    this.rerollCount = 0;
    this.tempRows = new Set();
    this.lockedRows = new Set();

    this.diceButtons = Array.from({ length: 5 }, (_, i) => document.getElementById(`dice${i + 1}`));
    this.rollBtn = document.getElementById("rollBtn");
    this.statusLabel = document.getElementById("status");
    this.table = document.getElementById("scoreTable");
    this.gameOverSent = false;
    this.initialize();
  }

  initialize() {
    this.rollBtn.addEventListener("click", () => this.setupRollButton());
    this.diceButtons.forEach((btn, i) => {
      btn.addEventListener("click", () => this.toggleDice(i));
    });
    this.buildScoreTable();
    this.updateTurnText();

    this.socket.onmessage = (event) => this.handleMessage(event.data);
    this.socket.send("REQUEST_NAME");
  }

  handleMessage(message) {
    const parts = message.split(" ");
    if (message === "REQUEST_NAME") {
      this.socket.send("NAME " + this.playerName);
    } else if (message.startsWith("START")) {
      this.opponentName = parts[2];
      this.buildScoreTable();
      this.statusLabel.textContent = "Game started!";
    } else if (message.startsWith("TURN")) {
      console.log("🔁 TURN mesajı alındı:", message);
      this.diceButtons.forEach(btn => btn.classList.remove("selected"));
      const playerTurnName = parts[1];

      if (parts.length === 4) {

        this.yourTurn = !(playerTurnName === this.playerName);
        this.rerollCount = 0;
        this.rollBtn.disabled = !this.yourTurn;
        this.rollBtn.textContent = "RE-ROLL (3)";
        this.statusLabel.textContent = this.yourTurn ? "Your turn!" : "Opponent's turn...";
        console.log("Turn message with row/val:", message);
        // Update the score for the specified row and value
        const row = parseInt(parts[2]);
        const val = parts[3];
        const cell = document.getElementById(`cell-opponent-${row}`);
        if (cell) {
          cell.textContent = val;
          cell.style.color = "black";
        }
        // ✅ Oyun bitiş kontrolü: Total (row 15) her iki oyuncuda da dolduysa, oyunu bitir
        if (this.lockedRows.size >= 16) {
          const myTotal = document.getElementById("cell-you-15").textContent;
          const oppTotal = document.getElementById("cell-opponent-15").textContent;
          if (myTotal && oppTotal) {
            const finalScore = parseInt(myTotal);
            console.log("Both totals present. Sending GAME_OVER", finalScore);
            this.socket.send("GAME_OVER " + finalScore);
          }
        }
      } else if (parts.length === 2) {
        console.log("Turn message with no row/val:", message);
        // Reset dice and scores for the new turn

        this.yourTurn = (playerTurnName === this.playerName);
        this.rerollCount = 0;
        this.rollBtn.disabled = !this.yourTurn;
        this.rollBtn.textContent = "RE-ROLL (3)";
        this.statusLabel.textContent = this.yourTurn ? "Your turn!" : "Opponent's turn...";
      }
    } else if (message.startsWith("DICE ")) {
      this.diceValues = parts[1].split("").map(Number);
      this.showDice();
    } else if (message.startsWith("SCORE_LOCKED")) {
      const row = parseInt(parts[1]);
      const player = parts[2];
      const val = parts[3];

      const cellId = (player === this.playerName)
        ? `cell-you-${row}`
        : `cell-opponent-${row}`;

      const cell = document.getElementById(cellId);
      if (cell) {
        cell.textContent = val;           // 0 olsa bile yaz
        cell.style.color = "black";
        if (player === this.playerName) this.lockedRows.add(row);
      }
    } else if (message.startsWith("GAME_RESULT")) {
      const parts = message.split(" ");
      const finalScore = parts[1];

      const promptMsg = `Game over!\nYour final score: ${finalScore}\n\nDo you want to restart the game?`;

      if (confirm(promptMsg)) {
        this.socket.send("RESTART");
      } else {
        this.socket.send("EXIT");
        alert("You exited the game. Returning to login...");
        location.reload(); // Login ekranına geri dön
      }
    } else if (message.startsWith("GAME_RESULT")) {
      const parts = message.split(" ");
      const result = parts[1];
      const finalScore = parts[2];

      let messageText = "";

      if (result === "WIN") {
        messageText = `🎉 You win! Your final score: ${finalScore}`;
      } else if (result === "LOSE") {
        messageText = `😢 You lost. Opponent's score: ${finalScore}`;
      } else {
        messageText = `🤝 It's a draw! Both scored: ${finalScore}`;
      }

      // Show restart prompt
      const restart = confirm(messageText + "\nDo you want to play again?");
      if (restart) {
        this.socket.send("RESTART");
      } else {
        this.socket.send("EXIT");
        location.reload(); // Or redirect to login screen
      }
    }
  }





  updateTurnText() {
    this.statusLabel.textContent = this.yourTurn ? "YOUR TURN" : "WAIT FOR YOUR TURN";
  }

  isMyTurn() {
    return this.yourTurn;
  }

  getPlayerColumn() {
    return 1;
  }

  setupRollButton() {
    if (!this.isMyTurn()) return alert("It's not your turn!");
    if (this.rerollCount >= 3) return alert("You have used all re-roll chances.");

    this.diceValues = this.diceValues.map((val, i) =>
      this.diceButtons[i].classList.contains("selected") ? val : Math.floor(Math.random() * 6) + 1
    );

    this.showDice();
    this.rerollCount++;
    this.rollBtn.textContent = `RE-ROLL (${3 - this.rerollCount})`;
    this.highlightPossibleScores();

    this.socket.send("ROLL_DICE " + this.diceValues.join(""));

  }

  toggleDice(index) {
    if (this.rerollCount < 1) {
      alert("You must roll the dice first!");
      return;
    }
    this.diceButtons[index].classList.toggle("selected");
  }

  showDice() {
    this.diceButtons.forEach((btn, i) => {
      btn.src = `dice${this.diceValues[i]}.png`;
    });
  }

  buildScoreTable() {
    const tbody = document.getElementById("scoreBody");
    const categories = [
      "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
      "Bonus", "Sum", "Three of a kind", "Four of a kind", "Full house",
      "Small straight", "Large straight", "Chance", "YAHTZEE", "Total"
    ];

    tbody.innerHTML = "";
    categories.forEach((cat, row) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `<td>${cat}</td><td id='cell-you-${row}'></td><td id='cell-opponent-${row}'></td>`;
      tr.querySelector(`#cell-you-${row}`)?.addEventListener("click", () => this.lockScore(row));
      tbody.appendChild(tr);
    });
  }

  highlightPossibleScores() {
    const points = this.getAllPoints(this.diceValues);
    this.tempRows.clear();

    const validScoringRows = [];

    // Sadece skor girilebilir satırları kontrol et (0–5, 8–14)
    for (let row = 0; row < 15; row++) {
      if (row === 6 || row === 7 || this.lockedRows.has(row)) continue;
      const score = points[row];
      if (score > 0) validScoringRows.push(row);
    }

    // Önce tüm hücreleri temizle (sadece scorlanabilir olanları)
    for (let row = 0; row < 15; row++) {
      if (row === 6 || row === 7 || this.lockedRows.has(row)) continue;
      const cell = document.getElementById(`cell-you-${row}`);
      if (cell) {
        cell.textContent = "";
        cell.style.color = "black";
      }
    }

    // Pozitif skor varsa sadece bunları göster
    if (validScoringRows.length > 0) {
      validScoringRows.forEach(row => {
        const cell = document.getElementById(`cell-you-${row}`);
        if (cell) {
          cell.textContent = points[row];
          cell.style.color = "red";
          this.tempRows.add(row);
        }
      });
    }
    // Aksi halde tüm scorlanabilir ama boş kalan satırlara 0 yaz
    else {
      for (let row = 0; row < 15; row++) {
        if (row === 6 || row === 7 || this.lockedRows.has(row)) continue;
        const cell = document.getElementById(`cell-you-${row}`);
        if (cell) {
          cell.textContent = "0";
          cell.style.color = "red";
          this.tempRows.add(row);
        }
      }
    }
  }

  getAllPoints(dice) {
    const counts = Array(7).fill(0);
    dice.forEach(d => counts[d]++);
    const total = dice.reduce((a, b) => a + b, 0);
    const scores = Array(15).fill(0);
    for (let i = 1; i <= 6; i++) scores[i - 1] = counts[i] * i;
    scores[8] = counts.some(c => c >= 3) ? total : 0;
    scores[9] = counts.some(c => c >= 4) ? total : 0;
    scores[10] = counts.filter(c => c === 3).length === 1 && counts.filter(c => c === 2).length === 1 ? 25 : 0;
    scores[11] = this.hasStraight(counts, 4) ? 30 : 0;
    scores[12] = this.hasStraight(counts, 5) ? 40 : 0;
    scores[13] = total;
    scores[14] = counts.includes(5) ? 50 : 0;
    return scores;
  }

  hasStraight(counts, length) {
    const needed = {
      4: [1, 1, 1, 1],
      5: [1, 1, 1, 1, 1],
    }[length];

    for (let i = 1; i <= 7 - needed.length; i++) {
      if (needed.every((_, j) => counts[i + j])) return true;
    }
    return false;
  }

  lockScore(row) {
    const val = document.getElementById(`cell-you-${row}`).textContent;
    if (!val || !this.tempRows.has(row)) return;

    this.tempRows.forEach(r => {
      if (r !== row) {
        const cell = document.getElementById(`cell-you-${r}`);
        if (cell) cell.textContent = "";
      }
    });

    document.getElementById(`cell-you-${row}`).textContent = val;
    document.getElementById(`cell-you-${row}`).style.color = "black";
    this.lockedRows.add(row);
    this.tempRows.clear();
    this.rerollCount = 0;
    this.rollBtn.textContent = "RE-ROLL (3)";

    this.socket.send(`SCORE_LOCKED ${row} ${val}`);
    this.updateSumAndBonus();
    this.socket.send(`TURN ${row} ${val}`);
    this.yourTurn = false;
    this.rollBtn.disabled = true;
    console.log("Locked row:", row);
    console.log("Locked rows size:", this.lockedRows.size);
    if (this.lockedRows.size >= 16) {
      console.log("Game over triggered. Sending final score:", this.calculateTotalScore());
      this.socket.send("GAME_OVER " + this.calculateTotalScore());
    }
  }

  updateSumAndBonus() {
    const upper = this.calculateUpperSum();
    const bonus = upper >= 63 ? 35 : 0;
    const total = this.calculateTotalScore() + bonus;

    document.getElementById("cell-you-6").textContent = bonus;
    document.getElementById("cell-you-7").textContent = upper;
    document.getElementById("cell-you-15").textContent = total;

    this.lockedRows.add(6);
    this.lockedRows.add(7);
    this.lockedRows.add(15);

    this.socket.send(`SCORE_LOCKED 6 ${bonus}`);
    this.socket.send(`SCORE_LOCKED 7 ${upper}`);
    this.socket.send(`SCORE_LOCKED 15 ${total}`);
  }

  calculateUpperSum() {
    let sum = 0;
    for (let i = 0; i <= 5; i++) {
      const val = parseInt(document.getElementById(`cell-you-${i}`).textContent || 0);
      sum += isNaN(val) ? 0 : val;
    }
    return sum;
  }

  calculateTotalScore() {
    let total = 0;
    for (let i = 0; i <= 14; i++) {
      const val = parseInt(document.getElementById(`cell-you-${i}`).textContent || 0);
      total += isNaN(val) ? 0 : val;
    }
    return total;
  }
}

// Export this class for use in game logic
export default YahtzeeGame;
