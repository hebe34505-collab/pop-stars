/**
 * 消灭星星的核心规则：
 * 1. 上下左右连成一片、颜色相同，而且至少 2 颗，才能消除。
 * 2. 得分 = 5 × 消除颗数 × 消除颗数。一次消得越多，分数越高。
 * 3. 消除后，上面的星星向下落，空出来的整列向左移动。
 */

let nextId = 1

// 生成一个铺满星星的网格。color 从 1 开始，方便对应页面上的颜色。
export function createBoard(rows, cols, colorCount) {
  const board = []
  for (let row = 0; row < rows; row++) {
    const line = []
    for (let col = 0; col < cols; col++) {
      line.push({
        id: nextId++,
        color: 1 + Math.floor(Math.random() * colorCount),
        removing: false
      })
    }
    board.push(line)
  }
  return board
}

// 开局至少要有一组可以消除的星星，否则玩家无法开始。
export function createPlayableBoard(rows, cols, colorCount) {
  let board = createBoard(rows, cols, colorCount)
  let times = 0
  while (!hasRemovableGroup(board) && times < 30) {
    board = createBoard(rows, cols, colorCount)
    times++
  }
  return board
}

export function calculateScore(count) {
  return 5 * count * count
}

// 从点击的位置出发，把上下左右相连的同色星星找出来。
export function findGroup(board, startRow, startCol) {
  const start = board[startRow] && board[startRow][startCol]
  if (!start) {
    return []
  }

  const rows = board.length
  const cols = board[0].length
  const color = start.color
  const visited = new Set()
  const queue = [[startRow, startCol]]
  const group = []

  while (queue.length > 0) {
    const [row, col] = queue.shift()
    const key = row + ',' + col
    if (visited.has(key)) {
      continue
    }
    visited.add(key)

    if (row < 0 || col < 0 || row >= rows || col >= cols) {
      continue
    }
    const cell = board[row][col]
    if (!cell || cell.color !== color) {
      continue
    }

    group.push([row, col])
    queue.push([row - 1, col], [row + 1, col], [row, col - 1], [row, col + 1])
  }

  return group
}

export function removeStars(board, group) {
  group.forEach(([row, col]) => {
    board[row][col] = null
  })
}

// 每一列各自下落，星星堆到这一列的最下面。
export function dropStars(board) {
  const rows = board.length
  const cols = board[0].length

  for (let col = 0; col < cols; col++) {
    const stars = []
    for (let row = 0; row < rows; row++) {
      if (board[row][col]) {
        stars.push(board[row][col])
      }
    }

    const emptyCount = rows - stars.length
    for (let row = 0; row < rows; row++) {
      board[row][col] = row < emptyCount ? null : stars[row - emptyCount]
    }
  }
}

// 把中间空掉的列去掉，右边的列整体向左靠。
export function moveColumnsLeft(board) {
  const rows = board.length
  const cols = board[0].length
  const next = []
  for (let row = 0; row < rows; row++) {
    next.push(Array(cols).fill(null))
  }

  let newCol = 0
  for (let col = 0; col < cols; col++) {
    let hasStar = false
    for (let row = 0; row < rows; row++) {
      if (board[row][col]) {
        hasStar = true
      }
    }
    if (!hasStar) {
      continue
    }
    for (let row = 0; row < rows; row++) {
      next[row][newCol] = board[row][col]
    }
    newCol++
  }

  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < cols; col++) {
      board[row][col] = next[row][col]
    }
  }
}

// 只要还有一组两颗及以上的同色星星，游戏就可以继续。
export function hasRemovableGroup(board) {
  const rows = board.length
  const cols = board[0].length
  const seen = []
  for (let row = 0; row < rows; row++) {
    seen.push(Array(cols).fill(false))
  }

  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < cols; col++) {
      if (!board[row][col] || seen[row][col]) {
        continue
      }
      const group = findGroup(board, row, col)
      if (group.length >= 2) {
        return true
      }
      group.forEach(([groupRow, groupCol]) => {
        seen[groupRow][groupCol] = true
      })
    }
  }
  return false
}
