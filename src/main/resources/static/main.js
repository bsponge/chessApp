let size = 80           // size of a square
let images = new Map()  // map for images
let uuid                // player UUID
let side                // side (white or black)

let gameSession = []    // 2d array representing pieces on chessboard
for (let i = 0; i < 8; ++i) {
    gameSession[i] = []
    for (let j = 0; j < 8; ++j) {
        gameSession[i][j] = null
    }
}
for (let i = 0; i < 8; ++i) {
    gameSession[i][1] = "PAWNWHITE.png"
    gameSession[i][6] = "PAWNBLACK.png"
}
gameSession[0][0] = "ROOKWHITE.png"
gameSession[1][0] = "KNIGHTWHITE.png"
gameSession[2][0] = "BISHOPWHITE.png"
gameSession[3][0] = "QUEENWHITE.png"
gameSession[4][0] = "KINGWHITE.png"
gameSession[5][0] = "BISHOPWHITE.png"
gameSession[6][0] = "KNIGHTWHITE.png"
gameSession[7][0] = "ROOKWHITE.png"
gameSession[0][7] = "ROOKBLACK.png"
gameSession[1][7] = "KNIGHTBLACK.png"
gameSession[2][7] = "BISHOPBLACK.png"
gameSession[3][7] = "QUEENBLACK.png"
gameSession[4][7] = "KINGBLACK.png"
gameSession[5][7] = "BISHOPBLACK.png"
gameSession[6][7] = "KNIGHTBLACK.png"
gameSession[7][7] = "ROOKBLACK.png"


let domain = window.location.href.slice(-1) != "#" ? window.location.href : window.location.href.slice(0, -1)
let gameSessionId = ""
let sock = new SockJS(domain + "chess")
let stompClient = Stomp.over(sock)
stompClient.connect({}, function(frame) {
    console.log("connected: " + frame)
})
let playerInfo;
let positions = []


let list = `BISHOPBLACK.png
BISHOPWHITE.png
KINGBLACK.png
KINGWHITE.png
KNIGHTBLACK.png
KNIGHTWHITE.png
PAWNBLACK.png
PAWNWHITE.png
QUEENBLACK.png
QUEENWHITE.png
ROOKBLACK.png
ROOKWHITE.png`.split("\n")
let ctx = document.getElementById('pieces').getContext('2d')
let timeElement = document.getElementById("panel")
ctx.canvas.width = size * 8
ctx.canvas.height = size * 8


preloadAllImages()

$.get("/reload", function(data, status) {
    if (data != null) {
        data = JSON.parse(data).pieces
        console.log("przyszlo")
        console.log(data)
        for (let i = 0; i < 8; ++i) {
            for (let j = 0; j < 8; ++j) {
                gameSession[i][j] = null
            }
        }
        for (let i = 0; i < data.length; ++i) {
            if (data[i] != null) {
                gameSession[data[i].x][data[i].y] = data[i].type + data[i].color + ".png"
            }
        }
        $.get("/getInfo", function(data, status) {
            if (data != null) {
                data = JSON.parse(data)
                side = data.side
            }
        })
        console.log("brefore drawing pieces")
        console.log(gameSession)
    }
})


function sendMsg() {
    $.get("/getGameSessionId", function(data, status) {
        if (status == "success") {
            if (data != null && data != "") {
                let move = {
                    msgType: 1,
                    id: data,
                    move: {
                        fromX: 0,
                        fromY: 1,
                        toX: 0,
                        toY: 2,
                        fromPiece: null,
                        toPiece: null,
                        doable: false
                    }
                }
                //stompClient.send("/app/chess/" + data, {}, JSON.stringify(move))
            }
        }
    })
}

function findGame() {
    $.get("/getId", function(data, status) {
        uuid = data
        if (status == "success") {
            $.post("/findGame", data, function(data, status) {
                if (data != null && typeof data != "undefined") {
                    data = JSON.parse(data)
                    if (uuid == data.whiteSide) {
                        side = "white"
                        console.log(side)
                    } else {
                        side = "black"
                        console.log(side)
                    }
                    drawChessboard()
                }
                drawPieces()
                console.log(uuid)
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
                console.log("move message received")
                drawPieces(msg)
                if (side == "white" && msg.checkOnWhtie) {
                    document.body.style.backgroundColor = "red";
                } else if (side == "white") {
                    document.body.style.backgroundColor = "black";
                } else if (side == "black" && msg.checkOnBlack) {
                    document.body.style.backgroundColor = "red";
                } else {
                    document.body.style.backgroundColor = "black";
                }
                break
            case 2:
                console.log("player info received")
                break
            case 3:
                console.log("Undo move received")
                drawPieces(msg)
                break;
            default:
        }
    } else {
        console.log("onReceivedMessage error")
    }
}

function subscribeToGame() {
    $.get("/getGameSessionId", function(data, status) {
        if (status == "success") {
            if (data != null) {
                gameSessionId = data
                stompClient.subscribe("/topic/messages/" + gameSessionId, onReceivedMessage)
                drawPieces()
            } else {
                console.log("gameSessionId = null")
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

function drawChessboard() {
    let ctx = document.getElementById('chessboard').getContext('2d')
    ctx.canvas.width = size * 8
    ctx.canvas.height = size * 8
    const light = "#EFE9CF"
    const dark = "#E8C15F"
    let bool = true

    for (let i = 0; i < 8; i++) {
        for (let j = 0; j < 8; j++) {
            if (bool) {
                ctx.fillStyle = light
            } else {
                ctx.fillStyle = dark
            }
            ctx.fillRect(j * size, i * size, size, size)
            bool = !bool
        }
        bool = !bool
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
        let mousePosition = getMousePosition(c, evt)
        if (positions.length == 2) {
            positions.pop()
            positions.pop()
        }

        positions.push([Math.floor(mousePosition.x / size), Math.floor(mousePosition.y / size)])

        let res = []
        res.pop()

        if (typeof side !== "undefined" && side == "white" && positions.length == 2) {
            let obj = {msgType:1,id: gameSessionId, playerId: uuid, isUndo:false, move:{fromX:positions[0][0],fromY:7-positions[0][1], toX:positions[1][0], toY:7-positions[1][1], fromPiece: null, toPiece: null, doable: false}, isCheckOnWhite: false, isCheckOnBlack: false, isMateOnWhite: false, isMateOnBlack: false}
            console.log(obj)
            stompClient.send("/app/chess/" + gameSessionId, {}, JSON.stringify(obj))
        } else if (typeof side !== "undefined" && side == "black" && positions.length == 2) {
            let obj = {msgType:1,id: gameSessionId, playerId: uuid, isUndo:false, move:{fromX:7-positions[0][0],fromY:positions[0][1], toX:7-positions[1][0], toY:positions[1][1], fromPiece: null, toPiece: null, doable: false}, isCheckOnWhite: false, isCheckOnBlack: false, isMateOnWhite: false, isMateOnBlack: false}
            console.log(obj)
            stompClient.send("/app/chess/" + gameSessionId, {}, JSON.stringify(obj))
        }
    })
}

function drawPieces(data) {
    let ctx = document.getElementById('pieces').getContext('2d')
    if (data != null) {
        if (data.castle) {
            if (data.move.toX == 2) {
                gameSession[3][data.move.fromY] = gameSession[0][data.move.fromY]
                gameSession[0][data.move.fromY] = null
            } else {
                gameSession[5][data.move.fromY] = gameSession[7][data.move.fromY]
                gameSession[7][data.move.fromY] = null
            }
        }
        data = data.move

        gameSession[data.toX][data.toY] = gameSession[data.fromX][data.fromY]
        gameSession[data.fromX][data.fromY] = null
    }

    if (gameSession != null) {
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
        if (side == "white") {
            for (let i = 0; i < 8; ++i) {
                for (let j = 0; j < 8; ++j) {
                    if (gameSession[i][j] != null) {
                        ctx.drawImage(images.get(gameSession[i][j]), i * size, (7-j) * size, size, size)
                    }
                }
            }
        } else {
            for (let i = 0; i < 8; ++i) {
                for (let j = 0; j < 8; ++j) {
                    if (gameSession[i][j] != null) {
                        ctx.drawImage(images.get(gameSession[i][j]), (7 - i) * size, j * size, size, size)
                    }
                }
            }
        }
    }
}
