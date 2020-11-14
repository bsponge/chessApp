
let size = 80
let images = new Map()

let domain = window.location.href
let gameSessionId = "";
//console.log(domain)
let sock = new SockJS(domain + "chess")
let stompClient = Stomp.over(sock)
stompClient.connect({}, function(frame) {
    console.log("connected: " + frame)
})

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

preloadAllImages(drawPieces)

//const id = setInterval(findGame, 300)
let refreshInterval = 10


//let chessboard = setInterval(drawChessboard, 300)

//setInterval(drawPieces, 100)
//setInterval(timeDisplay, refreshInterval)

function sendMsg() {
    $.get("/getGameSessionId", function(data, status) {
        if (status == "success") {
            if (data != null && data != "") {
                let move = {
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
                stompClient.send("/app/chess/" + data, {}, JSON.stringify(move))
            }
        }
    })
}

function findGame() {
    $.get("/getId", function(data, status) {
        if (status == "success") {
            $.post("/findGame", data, function(data, status) {
                subscribeToGame()
            })
        }
    })
}

function subscribeToGame() {
    $.get("/getGameSessionId", function(data, status) {
        if (status == "success") {
            if (data != null) {
                gameSessionId = data
                stompClient.subscribe("/topic/messages/" + gameSessionId)
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
    let player = fetch(playerInfoUrl)

    player.then(function(response) {
        return response.json()
    }).then(function(data) {
        playerInfo = data
    })
    if (playerInfo.side != null) {
        //console.log(playerInfo.side)
        if (playerInfo.side == "WHITE") {
            bool = true
        } else {
            bool = false
        }
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

    if (playerInfo.side != null) {
        clearInterval(chessboard)
    }
}

function preloadAllImages(callback) {
    for (let name of list) {
        let img = new Image()
        images.set(name, img)
        img.src= name
        img.onload = function() {
            callback()
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
        let player = fetch(playerInfoUrl)

        player.then(function(response) {
            return response.json()
        }).then(function(data) {
            playerInfo = data
        })
        positions.push([Math.floor(mousePosition.y / size), Math.floor(mousePosition.x / size)])

        let res = []
        res.pop()

        if (typeof playerInfo.side !== "undefined" && playerInfo.side == "WHITE" && positions.length == 2) {
            fetch(moveUrl
                + positions[0][0] + "/"
                + positions[0][1] + "/"
                + positions[1][0] + "/"
                + positions[1][1])
                .then(function(response) {
                    res.push(response.status)
                    if (response.status == 200) {
                        drawPieces(true)
                    } else if (response.data == "MATE") {
                        document.body.style.backgroundColor = "red"
                    }
                })
        } else if (typeof playerInfo.side !== "undefined" && playerInfo.side == "BLACK" && positions.length == 2) {
            fetch(moveUrl
                + ( 7 - positions[0][0]) + "/"
                + (positions[0][1]) + "/"
                + ( 7 - positions[1][0]) + "/"
                + (positions[1][1]))
                .then(function(response) {
                    res.push(response.status)
                    if (response.status == 200) {
                        drawPieces(true)
                    }
                })
        }
    })
}

function drawPieces() {
    let ctx = document.getElementById('pieces').getContext('2d')
    fetch(playerInfoUrl)
        .then(function(response) {
            return response.json()
        }).then(function(data) {
        playerInfo = data
    })
    fetch(gameUrl)
        .then(function(response) {
            return response.json()
        }).then(function(data) {
        gameSession = data
    })
    if (typeof gameSession.pieces != "undefined" && gameSession.pieces != null && playerInfo.side != null && gameSession != null) {
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
        //console.log(gameSession.checkMate)
        if (playerInfo.side == "WHITE" && gameSession.checkOnWhite == true) {
            document.body.style.backgroundColor = "red"
            document.getElementById("text").innerText = "CHECK"
        } else if (playerInfo.side == "BLACK" && gameSession.checkOnBlack == true) {
            document.body.style.backgroundColor = "red"
            document.getElementById("text").innerText = "CHECK"
        } else if (playerInfo.side == "WHITE" && gameSession.checkOnWhite == false) {
            document.body.style.backgroundColor = "black"
            document.getElementById("text").innerText = ""
        } else if (playerInfo.side == "BLACK" && gameSession.checkOnWhite == false) {
            document.body.style.backgroundColor = "black"
            document.getElementById("text").innerText = ""
        }
        //console.log(gameSession.checkMate)
        if (gameSession.checkMate == true) {
            //console.log("CHECKMATE")
            document.getElementById("text").innerText = "CHECKMATE"
        }
        if (playerInfo.side == "WHITE") {
            for (let piece of gameSession.pieces) {
                    if (piece.type != null) {
                        ctx.drawImage(images.get(piece.type + piece.color + ".png"), piece.y * size, piece.x * size, size, size)
                    }
            }
        } else {
            for (let piece of gameSession.pieces) {
                    if (piece.type !== null) {
                        ctx.drawImage(images.get(piece.type + piece.color + ".png"), piece.y * size, (7 - piece.x) * size, size, size)
                    }
            }
        }
    }
}
