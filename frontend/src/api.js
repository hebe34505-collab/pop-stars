const TOKEN_KEY = 'popstars-token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

// 统一发请求。登录后会自动带上令牌。
export async function request(url, options = {}) {
  const headers = { ...(options.headers || {}) }
  if (options.body) {
    headers['Content-Type'] = 'application/json'
  }
  const token = getToken()
  if (token) {
    headers.Authorization = 'Bearer ' + token
  }

  const response = await fetch(url, { ...options, headers })
  const text = await response.text()
  const data = text ? JSON.parse(text) : null
  if (!response.ok) {
    const error = new Error(data && data.message ? data.message : '请求失败')
    error.status = response.status
    throw error
  }
  return data
}
