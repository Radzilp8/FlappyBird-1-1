const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

// Game settings
const BOARD_WIDTH = 360;
const BOARD_HEIGHT = 640;
canvas.width = BOARD_WIDTH;
canvas.height = BOARD_HEIGHT;

// Bird settings
const BIRD_WIDTH = 34;
const BIRD_HEIGHT = 24;
let birdX = BOARD_WIDTH / 8;
let birdY = BOARD_HEIGHT / 2;
let velocityY = 0;
const gravity = 0.5;
let isJumping = false;

// Pipe settings
const PIPE_WIDTH = 64;
const PIPE_HEIGHT = 512;
let pipes = [];
let score = 0;
let gameOver = false;

// Load images
const birdImg = new Image();
birdImg.src = 'https://raw.githubusercontent.com/Radzilp8/FlappyBird-1-1/main/assets/images/flappybird.png';

const topPipeImg = new Image();
topPipeImg.src = 'https://raw.githubusercontent.com/Radzilp8/FlappyBird-1-1/main/assets/images/toppipe.png';

const bottomPipeImg = new Image();
bottomPipeImg.src = 'https://raw.githubusercontent.com/Radzilp8/FlappyBird-1-1/main/assets/images/bottompipe.png';

const backgroundImg = new Image();
backgroundImg.src = 'https://raw.githubusercontent.com/Radzilp8/FlappyBird-1-1/main/assets/images/flappybirdbg.png';

// Game logic
function drawBird() {
    ctx.drawImage(birdImg, birdX, birdY, BIRD_WIDTH, BIRD_HEIGHT);
}

function drawPipes() {
    pipes.forEach(pipe => {
        ctx.drawImage(pipe.img, pipe.x, pipe.y, PIPE_WIDTH, PIPE_HEIGHT);
    });
}

function movePipes() {
    pipes.forEach(pipe => {
        pipe.x -= 4; // Moving pipe to the left
        if (pipe.x + PIPE_WIDTH < 0) {
            pipes.splice(pipes.indexOf(pipe), 1);
            if (!pipe.passed) {
                score++;
                pipe.passed = true;
            }
        }
    });
}

function spawnPipes() {
    const gap = 150;
    const randomY = Math.floor(Math.random() * (BOARD_HEIGHT - gap));

    let topPipe = { x: BOARD_WIDTH, y: randomY - PIPE_HEIGHT, img: topPipeImg, passed: false };
    let bottomPipe = { x: BOARD_WIDTH, y: randomY + gap, img: bottomPipeImg, passed: false };

    pipes.push(topPipe, bottomPipe);
}

function drawScore() {
    ctx.fillStyle = 'white';
    ctx.font = '32px Arial';
    ctx.fillText(`Score: ${score}`, 10, 40);
}

function gameOverScreen() {
    ctx.fillStyle = 'white';
    ctx.font = '40px Arial';
    ctx.fillText('Game Over', BOARD_WIDTH / 4, BOARD_HEIGHT / 2);
}

function jump() {
    if (isJumping) {
        velocityY = -10;
        isJumping = false;
    }
}

function moveBird() {
    velocityY += gravity;
    birdY += velocityY;
    if (birdY > BOARD_HEIGHT - BIRD_HEIGHT || birdY < 0) {
        gameOver = true;
    }
}

function gameLoop() {
    if (gameOver) {
        gameOverScreen();
        return;
    }

    ctx.clearRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
    ctx.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT);

    drawBird();
    moveBird();
    movePipes();
    drawPipes();
    drawScore();

    requestAnimationFrame(gameLoop);
}

function startGame() {
    gameOver = false;
    score = 0;
    pipes = [];
    birdY = BOARD_HEIGHT / 2;
    velocityY = 0;
    gameLoop();
}

// Event listener for spacebar (to jump)
document.addEventListener('keydown', (e) => {
    if (e.code === 'Space') {
        if (gameOver) {
            startGame();
        } else {
            isJumping = true;
        }
    }
});

// Start the game
startGame();
