import { useState, useEffect, useCallback } from 'react'

const API_BASE = 'http://localhost:8080/api'
const TRACK_LENGTH = 100
const SAFE_DISTANCE = 5

function App() {
  const [positions, setPositions] = useState({ A: 0, B: 100 })
  const [targets, setTargets] = useState({ A: 0, B: 100 })
  const [collision, setCollision] = useState({ safe: true, message: '', distance: 100 })
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [successMsg, setSuccessMsg] = useState('')
  const [dispatchA, setDispatchA] = useState({ target: '', type: 'PICKUP', desc: '' })
  const [dispatchB, setDispatchB] = useState({ target: '', type: 'PICKUP', desc: '' })

  const fetchPositions = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/positions`)
      const data = await res.json()
      const posMap = {}
      const targetMap = {}
      data.forEach(item => {
        posMap[item.craneId] = item.position
        targetMap[item.craneId] = item.targetPosition !== undefined ? item.targetPosition : item.position
      })
      setPositions(prev => ({
        A: posMap.A !== undefined ? posMap.A : prev.A,
        B: posMap.B !== undefined ? posMap.B : prev.B,
      }))
      setTargets(prev => ({
        A: targetMap.A !== undefined ? targetMap.A : prev.A,
        B: targetMap.B !== undefined ? targetMap.B : prev.B,
      }))
    } catch {
      // backend not available, keep existing positions
    }
  }, [])

  const fetchCollision = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/collision-check`)
      const data = await res.json()
      setCollision(data)
    } catch {
      // keep existing state
    }
  }, [])

  const fetchTasks = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/tasks`)
      const data = await res.json()
      setTasks(data.slice(0, 20))
    } catch {
      // keep existing state
    }
  }, [])

  useEffect(() => {
    fetchPositions()
    fetchCollision()
    fetchTasks()
    const interval = setInterval(() => {
      fetchPositions()
      fetchCollision()
      fetchTasks()
    }, 2000)
    return () => clearInterval(interval)
  }, [fetchPositions, fetchCollision, fetchTasks])

  const handleDispatch = async (craneId) => {
    const dispatch = craneId === 'A' ? dispatchA : dispatchB
    const targetPos = parseFloat(dispatch.target)

    if (isNaN(targetPos) || targetPos < 0 || targetPos > TRACK_LENGTH) {
      setError(`目标位置必须在 0 - ${TRACK_LENGTH} 米之间`)
      setTimeout(() => setError(''), 3000)
      return
    }

    setLoading(true)
    setError('')
    setSuccessMsg('')

    try {
      const res = await fetch(`${API_BASE}/dispatch`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          craneId,
          targetPosition: targetPos,
          taskType: dispatch.type,
          description: dispatch.desc || `${craneId}行车前往${targetPos}米处`,
        }),
      })

      const data = await res.json()

      if (!res.ok) {
        setError(data.error || '调度失败')
        setTimeout(() => setError(''), 5000)
      } else {
        setSuccessMsg(`✅ 行车${craneId}已成功调度至 ${targetPos} 米处`)
        setTimeout(() => setSuccessMsg(''), 3000)
        if (craneId === 'A') setDispatchA({ target: '', type: 'PICKUP', desc: '' })
        else setDispatchB({ target: '', type: 'PICKUP', desc: '' })
        fetchPositions()
        fetchCollision()
        fetchTasks()
      }
    } catch {
      setError('无法连接到后端服务')
      setTimeout(() => setError(''), 3000)
    } finally {
      setLoading(false)
    }
  }

  const posToPercent = (pos) => (pos / TRACK_LENGTH) * 100

  const distance = Math.abs(positions.A - positions.B)
  const isDanger = distance < SAFE_DISTANCE

  const isMoving = (craneId) => Math.abs(positions[craneId] - targets[craneId]) > 0.1

  return (
    <div className="min-h-screen bg-gray-900 text-white p-4 md:p-8">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl md:text-4xl font-bold text-center mb-2">
          🏗️ 双向行车调度系统
        </h1>
        <p className="text-gray-400 text-center mb-8">
          实时监控两台行车在轨道上的位置，防止碰撞事故
        </p>

        {!collision.safe && (
          <div className="mb-6 p-4 bg-red-900/80 border-2 border-red-500 rounded-xl text-center animate-pulse">
            <span className="text-xl font-bold">🚨 {collision.message}</span>
          </div>
        )}
        {successMsg && (
          <div className="mb-6 p-4 bg-green-900/60 border border-green-500 rounded-xl text-center">
            <span className="text-lg">{successMsg}</span>
          </div>
        )}
        {error && (
          <div className="mb-6 p-4 bg-red-900/60 border border-red-500 rounded-xl text-center">
            <span className="text-lg">❌ {error}</span>
          </div>
        )}

        {/* Track Visualization */}
        <div className="bg-gray-800 rounded-2xl p-6 mb-6 shadow-xl">
          <h2 className="text-xl font-semibold mb-4">轨道实时视图</h2>

          <div className="relative mx-4 my-8">
            <div className="relative h-20 bg-gray-700 rounded-full overflow-visible">
              {/* Distance markers */}
              {[0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100].map(meter => (
                <div
                  key={meter}
                  className="absolute top-0 h-full flex flex-col items-center"
                  style={{ left: `${posToPercent(meter)}%`, transform: 'translateX(-50%)' }}
                >
                  <div className="w-px h-full bg-gray-600 opacity-50"></div>
                  <span className="absolute -bottom-6 text-xs text-gray-400 whitespace-nowrap">{meter}m</span>
                </div>
              ))}

              {/* Safe zone between cranes */}
              {!isDanger && (
                <div
                  className="absolute top-1 bottom-1 bg-green-900/30 rounded"
                  style={{
                    left: `${posToPercent(Math.min(positions.A, positions.B))}%`,
                    width: `${posToPercent(Math.max(positions.A, positions.B)) - posToPercent(Math.min(positions.A, positions.B))}%`,
                  }}
                ></div>
              )}

              {/* Danger zone */}
              {isDanger && (
                <div
                  className="absolute top-1 bottom-1 bg-red-900/50 rounded animate-pulse"
                  style={{
                    left: `${posToPercent(Math.min(positions.A, positions.B))}%`,
                    width: `${posToPercent(Math.max(positions.A, positions.B)) - posToPercent(Math.min(positions.A, positions.B))}%`,
                  }}
                ></div>
              )}

              {/* Crane A target line (dashed path) */}
              {isMoving('A') && (
                <div
                  className="absolute top-1/2 -translate-y-1/2 h-1 border-t-2 border-dashed border-blue-400 opacity-60"
                  style={{
                    left: `${posToPercent(Math.min(positions.A, targets.A))}%`,
                    width: `${posToPercent(Math.max(positions.A, targets.A)) - posToPercent(Math.min(positions.A, targets.A))}%`,
                  }}
                ></div>
              )}

              {/* Crane B target line (dashed path) */}
              {isMoving('B') && (
                <div
                  className="absolute top-1/2 -translate-y-1/2 h-1 border-t-2 border-dashed border-orange-400 opacity-60"
                  style={{
                    left: `${posToPercent(Math.min(positions.B, targets.B))}%`,
                    width: `${posToPercent(Math.max(positions.B, targets.B)) - posToPercent(Math.min(positions.B, targets.B))}%`,
                  }}
                ></div>
              )}

              {/* Crane A target marker */}
              {isMoving('A') && (
                <div
                  className="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 z-5"
                  style={{ left: `${posToPercent(targets.A)}%` }}
                >
                  <div className="w-6 h-6 border-2 border-dashed border-blue-400 rounded flex items-center justify-center text-xs text-blue-400">
                    🎯
                  </div>
                </div>
              )}

              {/* Crane B target marker */}
              {isMoving('B') && (
                <div
                  className="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 z-5"
                  style={{ left: `${posToPercent(targets.B)}%` }}
                >
                  <div className="w-6 h-6 border-2 border-dashed border-orange-400 rounded flex items-center justify-center text-xs text-orange-400">
                    🎯
                  </div>
                </div>
              )}

              {/* Crane A */}
              <div
                className="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 z-10 transition-all duration-700 ease-in-out"
                style={{ left: `${posToPercent(positions.A)}%` }}
              >
                <div className={`w-14 h-14 rounded-lg flex flex-col items-center justify-center text-sm font-bold shadow-lg border-2 ${isDanger ? 'bg-red-600 border-red-400 animate-bounce' : 'bg-blue-600 border-blue-400'}`}>
                  <span>A</span>
                  <span className="text-xs">{positions.A.toFixed(1)}m</span>
                </div>
              </div>

              {/* Crane B */}
              <div
                className="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 z-10 transition-all duration-700 ease-in-out"
                style={{ left: `${posToPercent(positions.B)}%` }}
              >
                <div className={`w-14 h-14 rounded-lg flex flex-col items-center justify-center text-sm font-bold shadow-lg border-2 ${isDanger ? 'bg-red-600 border-red-400 animate-bounce' : 'bg-orange-600 border-orange-400'}`}>
                  <span>B</span>
                  <span className="text-xs">{positions.B.toFixed(1)}m</span>
                </div>
              </div>
            </div>
          </div>

          {/* Status Info */}
          <div className="flex flex-wrap justify-center gap-6 mt-10 text-sm">
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 bg-blue-600 rounded"></div>
              <span>A 行车: <strong>{positions.A.toFixed(1)} m</strong>
                {isMoving('A') && <span className="text-blue-400 ml-1">→ 🎯 {targets.A.toFixed(1)} m</span>}
              </span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 bg-orange-600 rounded"></div>
              <span>B 行车: <strong>{positions.B.toFixed(1)} m</strong>
                {isMoving('B') && <span className="text-orange-400 ml-1">→ 🎯 {targets.B.toFixed(1)} m</span>}
              </span>
            </div>
            <div className="flex items-center gap-2">
              <div className={`w-4 h-4 ${isDanger ? 'bg-red-500' : 'bg-green-500'} rounded`}></div>
              <span>间距: <strong>{distance.toFixed(1)} m</strong>
                {isDanger && <span className="text-red-400 ml-1">⚠ 危险!</span>}
              </span>
            </div>
          </div>
        </div>

        {/* Dispatch Controls */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          {/* Crane A Dispatch */}
          <div className="bg-gray-800 rounded-2xl p-6 shadow-xl border-l-4 border-blue-500">
            <h3 className="text-lg font-semibold mb-4 text-blue-400">🚃 A 行车调度</h3>
            <div className="space-y-3">
              <div>
                <label className="block text-sm text-gray-400 mb-1">目标位置 (0-{TRACK_LENGTH}米)</label>
                <input
                  type="number"
                  min="0"
                  max={TRACK_LENGTH}
                  step="0.1"
                  value={dispatchA.target}
                  onChange={e => setDispatchA({ ...dispatchA, target: e.target.value })}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="例如: 50"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">任务类型</label>
                <select
                  value={dispatchA.type}
                  onChange={e => setDispatchA({ ...dispatchA, type: e.target.value })}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="PICKUP">接货</option>
                  <option value="DELIVER">送货</option>
                  <option value="MOVE">移动</option>
                  <option value="MAINTENANCE">维护</option>
                </select>
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">描述</label>
                <input
                  type="text"
                  value={dispatchA.desc}
                  onChange={e => setDispatchA({ ...dispatchA, desc: e.target.value })}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="例如: 去50米处接货"
                />
              </div>
              <button
                onClick={() => handleDispatch('A')}
                disabled={loading}
                className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-800 disabled:opacity-50 text-white font-semibold py-3 rounded-lg transition-colors cursor-pointer disabled:cursor-not-allowed"
              >
                {loading ? '调度中...' : '📋 指派 A 行车'}
              </button>
            </div>
          </div>

          {/* Crane B Dispatch */}
          <div className="bg-gray-800 rounded-2xl p-6 shadow-xl border-l-4 border-orange-500">
            <h3 className="text-lg font-semibold mb-4 text-orange-400">🚃 B 行车调度</h3>
            <div className="space-y-3">
              <div>
                <label className="block text-sm text-gray-400 mb-1">目标位置 (0-{TRACK_LENGTH}米)</label>
                <input
                  type="number"
                  min="0"
                  max={TRACK_LENGTH}
                  step="0.1"
                  value={dispatchB.target}
                  onChange={e => setDispatchB({ ...dispatchB, target: e.target.value })}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="例如: 80"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">任务类型</label>
                <select
                  value={dispatchB.type}
                  onChange={e => setDispatchB({ ...dispatchB, type: e.target.value })}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                >
                  <option value="PICKUP">接货</option>
                  <option value="DELIVER">送货</option>
                  <option value="MOVE">移动</option>
                  <option value="MAINTENANCE">维护</option>
                </select>
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">描述</label>
                <input
                  type="text"
                  value={dispatchB.desc}
                  onChange={e => setDispatchB({ ...dispatchB, desc: e.target.value })}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="例如: 去80米处送货"
                />
              </div>
              <button
                onClick={() => handleDispatch('B')}
                disabled={loading}
                className="w-full bg-orange-600 hover:bg-orange-700 disabled:bg-orange-800 disabled:opacity-50 text-white font-semibold py-3 rounded-lg transition-colors cursor-pointer disabled:cursor-not-allowed"
              >
                {loading ? '调度中...' : '📋 指派 B 行车'}
              </button>
            </div>
          </div>
        </div>

        {/* Task History */}
        <div className="bg-gray-800 rounded-2xl p-6 shadow-xl">
          <h2 className="text-xl font-semibold mb-4">📋 最近任务记录</h2>
          {tasks.length === 0 ? (
            <p className="text-gray-500 text-center py-4">暂无任务记录</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-gray-400 border-b border-gray-700">
                    <th className="text-left py-2 px-3">行车</th>
                    <th className="text-left py-2 px-3">目标位置</th>
                    <th className="text-left py-2 px-3">类型</th>
                    <th className="text-left py-2 px-3">描述</th>
                    <th className="text-left py-2 px-3">状态</th>
                    <th className="text-left py-2 px-3">创建时间</th>
                  </tr>
                </thead>
                <tbody>
                  {tasks.map(task => (
                    <tr key={task.id} className="border-b border-gray-700/50 hover:bg-gray-700/30">
                      <td className="py-2 px-3">
                        <span className={`inline-block w-6 h-6 rounded text-center text-xs font-bold leading-6 ${task.craneId === 'A' ? 'bg-blue-600' : 'bg-orange-600'}`}>
                          {task.craneId}
                        </span>
                      </td>
                      <td className="py-2 px-3">{task.targetPosition} m</td>
                      <td className="py-2 px-3">
                        <span className="px-2 py-0.5 bg-gray-700 rounded text-xs">
                          {task.taskType === 'PICKUP' && '接货'}
                          {task.taskType === 'DELIVER' && '送货'}
                          {task.taskType === 'MOVE' && '移动'}
                          {task.taskType === 'MAINTENANCE' && '维护'}
                        </span>
                      </td>
                      <td className="py-2 px-3 text-gray-300">{task.description || '-'}</td>
                      <td className="py-2 px-3">
                        <span className={`px-2 py-0.5 rounded text-xs ${task.status === 'COMPLETED' ? 'bg-green-900 text-green-300' : task.status === 'PENDING' ? 'bg-yellow-900 text-yellow-300' : 'bg-gray-700 text-gray-300'}`}>
                          {task.status === 'COMPLETED' && '已完成'}
                          {task.status === 'PENDING' && '待执行'}
                          {task.status === 'RUNNING' && '执行中'}
                        </span>
                      </td>
                      <td className="py-2 px-3 text-gray-400">{task.createdAt?.replace('T', ' ') || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <p className="text-center text-gray-600 text-xs mt-8">
          双向行车调度系统 · 安全距离: {SAFE_DISTANCE}m · 轨道长度: {TRACK_LENGTH}m
        </p>
      </div>
    </div>
  )
}

export default App
