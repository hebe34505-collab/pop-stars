import {
  calculateScore,
  createPlayableBoard,
  dropStars,
  findGroup,
  hasRemovableGroup,
  moveColumnsLeft,
  removeStars
} from './src/game.js'

function assert(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

function cell(id, color) {
  return { id, color, removing: false }
}

// 下落：星星应该落到每一列底部。
const dropBoard = [
  [cell(1, 1), null],
  [null, cell(2, 2)],
  [cell(3, 1), null]
]
dropStars(dropBoard)
assert(dropBoard[0][0] === null && dropBoard[0][1] === null, '下落后天空应该是空的')
assert(dropBoard[2][0].id === 3 && dropBoard[1][0].id === 1, '第一列两颗星星应堆在底部')
assert(dropBoard[2][1].id === 2 && dropBoard[1][1] === null, '第二列星星应落到最底部')

// 空列左移：中间空列消失后，右边的列补过来。
const shiftBoard = [
  [cell(1, 1), null, cell(3, 2)],
  [cell(2, 1), null, cell(4, 2)]
]
moveColumnsLeft(shiftBoard)
assert(shiftBoard[0][0].id === 1 && shiftBoard[0][1].id === 3 && shiftBoard[0][2] === null, '空列应向左靠拢')

// 同色相连才能消除，斜角不算。
const groupBoard = [
  [cell(1, 1), cell(2, 1), cell(3, 2)],
  [cell(4, 2), cell(5, 1), null]
]
const group = findGroup(groupBoard, 0, 0)
assert(group.length === 3, '应找到 3 颗相连的红色星星，实际 ' + group.length)
assert(calculateScore(3) === 45, '3 颗星星得分应为 45')
assert(findGroup(groupBoard, 1, 0).length === 1, '单独一颗不能和斜角连在一起')

removeStars(groupBoard, group)
assert(groupBoard[0][0] === null && groupBoard[0][1] === null && groupBoard[1][1] === null, '选中的星星应被清除')

const playable = createPlayableBoard(10, 10, 5)
assert(hasRemovableGroup(playable), '新棋盘应该可以开始消除')

// 模拟玩家总是消除当前最大的一组，看 20 步大概能得多少分。
const scores = []
for (let game = 0; game < 200; game++) {
  const board = createPlayableBoard(10, 10, 5)
  let score = 0
  for (let step = 0; step < 20; step++) {
    let best = []
    for (let row = 0; row < board.length; row++) {
      for (let col = 0; col < board[0].length; col++) {
        if (!board[row][col]) continue
        const found = findGroup(board, row, col)
        if (found.length > best.length) best = found
      }
    }
    if (best.length < 2) break
    score += calculateScore(best.length)
    removeStars(board, best)
    dropStars(board)
    moveColumnsLeft(board)
  }
  scores.push(score)
}
scores.sort((a, b) => a - b)
const average = Math.round(scores.reduce((sum, value) => sum + value, 0) / scores.length)
console.log(JSON.stringify({
  min: scores[0],
  p25: scores[50],
  median: scores[100],
  p75: scores[150],
  max: scores[scores.length - 1],
  average,
  reach1000: scores.filter((score) => score >= 1000).length
}))
