let size = 80           // size of a square
let images = new Map()  // map for images
let uuid                // player UUID
let side                // side (white or black)
let turn = true         // true == WHITE    false == BLACK
let whiteTime = 120_000      // in milliseconds
let blackTime = 120_000
let isPromotionMenuOn = false
let promotionChoicePosition = []
let whiteTimer
let blackTimer
let gameSession = []    // 2d array representing pieces on chessboard
for (let i = 0; i < 8; ++i) {
    gameSession[i] = []
    for (let j = 0; j < 8; ++j) {
        gameSession[i][j] = null
    }
}
for (let i = 0; i < 8; ++i) {
    gameSession[i][1] = "PAWN_WHITE.png"
    gameSession[i][6] = "PAWN_BLACK.png"
}
gameSession[0][0] = "ROOK_WHITE.png"
gameSession[1][0] = "KNIGHT_WHITE.png"
gameSession[2][0] = "BISHOP_WHITE.png"
gameSession[3][0] = "QUEEN_WHITE.png"
gameSession[4][0] = "KING_WHITE.png"
gameSession[5][0] = "BISHOP_WHITE.png"
gameSession[6][0] = "KNIGHT_WHITE.png"
gameSession[7][0] = "ROOK_WHITE.png"
gameSession[0][7] = "ROOK_BLACK.png"
gameSession[1][7] = "KNIGHT_BLACK.png"
gameSession[2][7] = "BISHOP_BLACK.png"
gameSession[3][7] = "QUEEN_BLACK.png"
gameSession[4][7] = "KING_BLACK.png"
gameSession[5][7] = "BISHOP_BLACK.png"
gameSession[6][7] = "KNIGHT_BLACK.png"
gameSession[7][7] = "ROOK_BLACK.png"

const PAWN = 1
const BISHOP = 2;
const KNIGHT = 4;
const ROOK = 8;
const QUEEN = 16;
const KING = 32;

const WHITE = 64
const BLACK = 128

const pieces = new Map()
pieces[PAWN] = "PAWN"
pieces[BISHOP] = "BISHOP"
pieces[KNIGHT] = "KNIGHT"
pieces[ROOK] = "ROOK"
pieces[QUEEN] = "QUEEN"
pieces[KING] = "KING"

let domain = window.location.href.slice(-1) !== "#" ? window.location.href : window.location.href.slice(0, -1)
let gameSessionId = ""
let sock = new SockJS(domain + "chess")

let stompClient = Stomp.over(sock)
stompClient.connect({}, function(frame) {
    console.log("connected: " + frame)
})
let positions = []


let list = `BISHOP_BLACK.png
BISHOP_WHITE.png
KING_BLACK.png
KING_WHITE.png
KNIGHT_BLACK.png
KNIGHT_WHITE.png
PAWN_BLACK.png
PAWN_WHITE.png
QUEEN_BLACK.png
QUEEN_WHITE.png
ROOK_BLACK.png
ROOK_WHITE.png`.split("\n")
let ctx = document.getElementById('pieces').getContext('2d')
document.getElementById("time");
ctx.canvas.width = size * 8
ctx.canvas.height = size * 8


preloadAllImages()

/*
$.get("/reload", function(data, status) {
    if (data != null && data !== "null") {
        data = JSON.parse(data)
        for (let i = 0; i < 8; ++i) {
            for (let j = 0; j < 8; ++j) {
                gameSession[i][j] = null
            }
        }
        for (let i = 0; i < data.length; ++i) {
            if (data[i] != null) {
                gameSession[data[i].x][data[i].y] = data[i].type + "_" + data[i].color + ".png"
            }
        }
        $.get("/getInfo", function(data, status) {
            if (data != null) {
                $.get("/getId", function(data, status) {
                    uuid = data
                })
                subscribeToGame()
                data = JSON.parse(data)
                side = data.side
                drawPieces(null)
            }
        })
    }
})

 */

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

function findGame() {
    $.get("/getId", function(data, status) {
        uuid = data
        if (status === "success") {
            $.post("/findGame", data, function(data) {
                if (data != null && typeof data != "undefined") {
                    data = JSON.parse(data)
                    if (uuid === data.whiteSide) {
                        side = "white"
                        gameSessionId = getCookie("gameUuid")
                        console.log(side)
                    } else {
                        side = "black"
                        gameSessionId = getCookie("gameUuid")
                        console.log(side)
                    }
                }
                window.location.replace("game?g=" + getCookie("gameUuid"))
                console.log("co jest");
                subscribeToGame()
            })
        }
    })
}

function onReceivedMessage(msg) {
    if (msg != null && typeof msg != "undefined") {
        msg = JSON.parse(msg.body)
        console.log(msg)
        switch (msg.msgType) {
            case 0:

                break
            case 1:
                if (whiteTimer == null) {
                    whiteTimer = setInterval(function() {
                        if (turn) {
                            document.getElementById("whiteTime").innerHTML = "White: " + String(Math.floor((whiteTime / 1000) / 60)) + ":" + String(Math.floor((whiteTime / 1000) % 60))
                            whiteTime -= 100
                        }
                    }, 100)
                    blackTimer = setInterval(function() {
                        if (!turn) {
                            document.getElementById("blackTime").innerHTML = "Black: " + String(Math.floor((blackTime / 1000) / 60)) + ":" + String(Math.floor((blackTime / 1000) % 60))
                            blackTime -= 100
                        }
                    }, 100)
                }
                turn = !turn
                drawPieces(msg)
                if (side === "white" && msg.checkOnWhtie) {
                    document.body.style.backgroundColor = "red";
                } else if (side === "white") {
                    document.body.style.backgroundColor = "black";
                } else if (side === "black" && msg.checkOnBlack) {
                    document.body.style.backgroundColor = "red";
                } else {
                    document.body.style.backgroundColor = "black";
                }
                break
            case 2:
                break
            default:
        }
    } else {
        console.log("onReceivedMessage error")
    }
}

function subscribeToGame() {
    $.get("/getGameSessionId", function(data, status) {
        if (status === "success") {
            if (data != null) {
                stompClient.subscribe("/topic/messages/" + gameSessionId, onReceivedMessage)
                drawPieces()
            }
        }
    })
}

function getMousePosition(canvas, evt) {
    let c = document.getElementById('pieces').getBoundingClientRect()
    return {
        x: evt.clientX - c.left,
        y: evt.clientY - c.top
    }
}

function drawChessboard(brightness = 100) {
    let ctx = document.getElementById('chessboard').getContext('2d')
    ctx.canvas.width = size * 8
    ctx.canvas.height = size * 8
    const light = "#EFE9CF"
    const dark = "#E8C15F"
    let bool = true
    console.log("Drawing chessboard")

    for (let i = 0; i < 8; i++) {
        for (let j = 0; j < 8; j++) {
            if (bool) {
                ctx.fillStyle = light
            } else {
                ctx.fillStyle = dark
            }
            ctx.filter = `brightness(${brightness}%)`
            ctx.fillRect(j * size, i * size, size, size)
            bool = !bool
        }
        bool = !bool
    }
}

function displayPromotionMenu(x, y) {
    drawChessboard(40)
    drawPieces(null, 40)
    let ctx = document.getElementById('pieces').getContext('2d')
    ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
    if (side === "white") {
        ctx.filter = 'brightness(100%)'
        ctx.drawImage(images.get("QUEEN_WHITE.png"), x * size, (7 - y) * size, size, size)
        ctx.drawImage(images.get("ROOK_WHITE.png"), x * size, (7 - y + 1) * size, size, size)
        ctx.drawImage(images.get("KNIGHT_WHITE.png"), x * size, (7 - y + 2) * size, size, size)
        ctx.drawImage(images.get("BISHOP_WHITE.png"), x * size, (7 - y + 3) * size, size, size)
    } else {
        ctx.filter = 'brightness(100%)'
        ctx.drawImage(images.get("QUEEN_BLACK.png"), (7 - x) * size, y * size, size, size)
        ctx.drawImage(images.get("ROOK_BLACK.png"), (7 - x) * size, (y + 1) * size, size, size)
        ctx.drawImage(images.get("KNIGHT_BLACK.png"), (7 - x) * size, (y + 2) * size, size, size)
        ctx.drawImage(images.get("BISHOP_BLACK.png"), (7 - x) * size, (y + 3) * size, size, size)
    }
}

function preloadAllImages() {
    for (let name of list) {
        let img = new Image()
        images.set(name, img)
        img.src= name
        img.onload = function() {

        }
    }
    let ctx = document.getElementById('pieces').getContext('2d')
    ctx.canvas.width = size * 8
    ctx.canvas.height = size * 8
    let c = document.getElementById('pieces').addEventListener('click', function(evt) {
        let promotion
        if (!isPromotionMenuOn) {
            let mousePosition = getMousePosition(c, evt)
            if (positions.length === 2) {
                positions.pop()
                positions.pop()
            }
            positions.push([Math.floor(mousePosition.x / size), Math.floor(mousePosition.y / size)])
            if (side === "white" && positions.length === 2) {
                console.log("1.")
                console.log(gameSession[positions[0][0]][7-positions[0][1]] !== null)
                console.log("2.")
                console.log(gameSession[positions[0][0]][7-positions[0][1]].includes("PAWN"))
                console.log("3.")
                console.log(7-positions[1][1] === 7)
                if (gameSession[positions[0][0]][7-positions[0][1]] !== null
                    && gameSession[positions[0][0]][7-positions[0][1]].includes("PAWN")
                    && 7 - positions[1][1] === 7) {
                    isPromotionMenuOn = true;
                    displayPromotionMenu(positions[1][0], 7 - positions[1][1])
                    return
                }
                let obj = {msgType:1,
                    id: gameSessionId,
                    playerId: uuid,
                    undo: 0,
                    move:
                        {   fromX: positions[0][0],
                            fromY: 7-positions[0][1],
                            toX: positions[1][0],
                            toY: 7-positions[1][1]
                        },
                }
                stompClient.send("/app/chess/" + gameSessionId, {}, JSON.stringify(obj))
            } else if (side === "black" && positions.length === 2) {
                if (gameSession[7-positions[0][0]][positions[0][1]] !== null
                    && gameSession[7-positions[0][0]][positions[0][1]].includes("PAWN")
                    && positions[1][1] === 0) {
                    isPromotionMenuOn = true;
                    displayPromotionMenu(7 - positions[1][0], positions[1][1])
                    return
                }
                let obj = {msgType:1,
                    id: gameSessionId,
                    playerId: uuid,
                    undo: 0,
                    move:
                        {   fromX:7-positions[0][0],
                            fromY:positions[0][1],
                            toX:7-positions[1][0],
                            toY:positions[1][1]
                        },
                }
                stompClient.send("/app/chess/" + gameSessionId, {}, JSON.stringify(obj))
            }
        } else {
            let mousePosition = getMousePosition(c, evt)
            promotionChoicePosition.push([Math.floor(mousePosition.x / size), Math.floor(mousePosition.y / size)])
            if (Math.floor(mousePosition.x / size) === positions[1][0]) {
                if (side === "white") {
                    switch (Math.floor(mousePosition.y / size)) {
                        case 0:
                            promotion = "QUEEN"
                            break
                        case 1:
                            promotion = "ROOK"
                            break
                        case 2:
                            promotion = "KNIGHT"
                            break
                        case 3:
                            promotion = "BISHOP"
                            break
                        default:
                            return
                    }
                    let obj = {msgType:1,
                        id: gameSessionId,
                        playerId: uuid,
                        isUndo: false,
                        move:
                            {   fromX: positions[0][0],
                                fromY: 7-positions[0][1],
                                toX: positions[1][0],
                                toY: 7-positions[1][1]
                            },
                        promotionType: promotion
                    }
                    stompClient.send("/app/chess/" + gameSessionId, {}, JSON.stringify(obj))
                } else {
                    switch (Math.floor(mousePosition.y / size)) {
                        case 0:
                            promotion = "QUEEN"
                            break
                        case 1:
                            promotion = "ROOK"
                            break
                        case 2:
                            promotion = "KNIGHT"
                            break
                        case 3:
                            promotion = "BISHOP"
                    }
                    let obj = {msgType:1,
                        id: gameSessionId,
                        playerId: uuid,
                        isUndo:false,
                        move:
                            {   fromX:7-positions[0][0],
                                fromY:positions[0][1],
                                toX:7-positions[1][0],
                                toY:positions[1][1]
                            },
                        promotionType: promotion
                    }
                    stompClient.send("/app/chess/" + gameSessionId, {}, JSON.stringify(obj))
                }
            }
            isPromotionMenuOn = false
            //drawChessboard()
            //drawPieces(null)
        }

    })
}

function undoMove() {
    $.get("/getId", function(data, status) {
        if (data != null && typeof data != "undefined") {
            let obj = {msgType:1,
                id: gameSessionId,
                playerId: data,
                undo: 1,
                move:
                    {fromX:0,
                        fromY:0,
                        toX:0,
                        toY:0},
                isCheckOnWhite: false,
                isCheckOnBlack: false,
                isMateOnWhite: false,
                isMateOnBlack: false}
            stompClient.send("/app/chess/" + gameSessionId, {}, JSON.stringify(obj))
        }
    })
}

function drawPieces(data, brightness = 100) {
    let ctx = document.getElementById('pieces').getContext('2d')
    if (data != null) {
        if (data.castle) {
            if (data.move.toX === 2) {
                gameSession[3][data.move.fromY] = gameSession[0][data.move.fromY]
                gameSession[0][data.move.fromY] = null
            } else {
                gameSession[5][data.move.fromY] = gameSession[7][data.move.fromY]
                gameSession[7][data.move.fromY] = null
            }
        }

        gameSession[data.move.toX][data.move.toY] = gameSession[data.move.fromX][data.move.fromY]
        gameSession[data.move.fromX][data.move.fromY] = null

        if (data.promotionType !== 0) {
            if (gameSession[data.move.toX][data.move.toY].includes("WHITE")) {
                gameSession[data.move.toX][data.move.toY] = pieces[data.promotionType] + "_" + "WHITE.png"
            } else {
                gameSession[data.move.toX][data.move.toY] = pieces[data.promotionType] + "_" + "BLACK.png"
            }
        }

        if (data.undo) {
            gameSession[data.move.fromX][data.move.fromY] = (pieces[data.move.fromPiece & 63]) + "_" + ((data.move.fromPiece & 192) === WHITE ? "WHITE" : "BLACK") + ".png"
            gameSession[data.move.toX][data.move.toY] = (pieces[data.move.toPiece & 63]) + "_" + ((data.move.toPiece & 192) === WHITE ? "WHITE" : "BLACK") + ".png"
        }
    }

    if (gameSession != null) {
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
        if (side === "white") {
            for (let i = 0; i < 8; ++i) {
                for (let j = 0; j < 8; ++j) {
                    if (gameSession[i][j] != null) {

                        ctx.filter = `brightness(${brightness}%)`
                        ctx.drawImage(images.get(gameSession[i][j]), i * size, (7-j) * size, size, size)
                    }
                }
            }
        } else {
            for (let i = 0; i < 8; ++i) {
                for (let j = 0; j < 8; ++j) {
                    if (gameSession[i][j] != null) {
                        ctx.filter = `brightness(${brightness}%)`
                        ctx.drawImage(images.get(gameSession[i][j]), (7 - i) * size, j * size, size, size)
                    }
                }
            }
        }
    }
}
