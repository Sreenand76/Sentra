import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Activity, Settings, ShieldAlert, ShieldCheck, Zap } from 'lucide-react';

const API_BASE = 'http://localhost:8080/api/v1';

function App() {
  const [config, setConfig] = useState({ capacity: 5, refillRate: 1 });
  const [logs, setLogs] = useState([]);
  const [responseMsg, setResponseMsg] = useState(null);

  const fetchConfig = async () => {
    try {
      const res = await axios.get(`${API_BASE}/config`);
      setConfig(res.data);
    } catch (err) {
      console.error('Failed to fetch config', err);
    }
  };

  const fetchLogs = async () => {
    try {
      const res = await axios.get(`${API_BASE}/logs`);
      setLogs(res.data);
    } catch (err) {
      console.error('Failed to fetch logs', err);
    }
  };

  useEffect(() => {
    fetchConfig();
    fetchLogs();
    const interval = setInterval(fetchLogs, 2000);
    return () => clearInterval(interval);
  }, []);

  const updateConfig = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API_BASE}/config?capacity=${config.capacity}&refillRate=${config.refillRate}`);
      setResponseMsg({ type: 'success', text: `Config Updated: Cap ${config.capacity}, Refill ${config.refillRate}/s` });
    } catch (err) {
      setResponseMsg({ type: 'error', text: 'Update Failed' });
    }
  };

  const sendRequest = async () => {
    try {
      const res = await axios.get(`${API_BASE}/secure-data`);
      setResponseMsg({ type: 'success', text: res.data.message });
      fetchLogs();
    } catch (err) {
      setResponseMsg({
        type: 'error',
        text: err.response?.data?.message || 'Request Failed'
      });
      fetchLogs();
    }
  };

  const sendBurst = async () => {

    setResponseMsg({
      type: "success",
      text: "Running k6 burst simulation..."
    });

    try {

      const res = await axios.get(`${API_BASE}/k6-test`);

      setResponseMsg({
        type: "success",
        text: "k6 traffic simulation completed"
      });

      console.log(res.data.output);

      fetchLogs();

    } catch (err) {

      setResponseMsg({
        type: "error",
        text: "k6 simulation failed"
      });
    }
  };

  return (
    <div className="min-h-screen bg-cyber-bg font-sans text-cyber-text p-8">
      <header className="mb-12 border-b border-cyber-border pb-6 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <ShieldAlert className="w-10 h-10 text-cyber-border" />
          <h1 className="text-4xl font-bold tracking-wider text-transparent bg-clip-text bg-gradient-to-r from-cyber-border to-cyber-neon">
            SENTRA
            <span className="text-cyber-text text-lg ml-3 font-mono font-light tracking-normal uppercase">Rate Limiter Engine</span>
          </h1>
        </div>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Control Panel */}
        <div className="col-span-1 space-y-8">
          <div className="bg-cyber-card border border-cyber-border/30 rounded-lg p-6 shadow-[0_0_15px_rgba(0,240,255,0.1)] relative overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-cyber-border to-transparent"></div>
            <h2 className="text-xl font-mono flex items-center gap-2 mb-6 text-cyber-border">
              <Settings size={20} /> CONFIGURATION
            </h2>
            <form onSubmit={updateConfig} className="space-y-4">
              <div>
                <label className="block text-sm text-gray-400 mb-1 font-mono">CAPACITY (Max Burst)</label>
                <input
                  type="number"
                  value={config.capacity}
                  onChange={e => setConfig({ ...config, capacity: e.target.value })}
                  className="w-full bg-black border border-cyber-border/50 rounded px-4 py-2 text-cyber-border focus:outline-none focus:border-cyber-border focus:ring-1 focus:ring-cyber-border font-mono"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1 font-mono">REFILL RATE (/sec)</label>
                <input
                  type="number"
                  value={config.refillRate}
                  onChange={e => setConfig({ ...config, refillRate: e.target.value })}
                  className="w-full bg-black border border-cyber-border/50 rounded px-4 py-2 text-cyber-border focus:outline-none focus:border-cyber-border focus:ring-1 focus:ring-cyber-border font-mono"
                />
              </div>
              <button type="submit" className="w-full bg-cyber-border/10 hover:bg-cyber-border/20 border border-cyber-border text-cyber-border py-2 rounded transition-all uppercase font-bold tracking-widest mt-4">
                Update Protocol
              </button>
            </form>
          </div>

          <div className="bg-cyber-card border border-cyber-neon/30 rounded-lg p-6 shadow-[0_0_15px_rgba(255,0,60,0.1)] relative overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-cyber-neon to-transparent"></div>
            <h2 className="text-xl font-mono flex items-center gap-2 mb-6 text-cyber-neon">
              <Zap size={20} /> TRAFFIC GENERATOR
            </h2>
            <div className="space-y-4">
              <button onClick={sendRequest} className="w-full bg-cyber-neon/10 hover:bg-cyber-neon/20 border border-cyber-neon text-cyber-neon py-3 rounded transition-all uppercase font-bold tracking-widest flex justify-center items-center gap-2 shadow-[0_0_10px_rgba(255,0,60,0.2)]">
                Send 1 Request
              </button>
              <button onClick={sendBurst} className="w-full bg-cyber-neon/20 hover:bg-cyber-neon/30 border border-cyber-neon text-white py-3 rounded transition-all uppercase font-bold tracking-widest flex justify-center items-center gap-2 shadow-[0_0_10px_rgba(255,0,60,0.4)]">
                Send Burst Request
              </button>
            </div>

            {responseMsg && (
              <div className={`mt-6 p-4 border rounded font-mono text-sm ${responseMsg.type === 'success' ? 'border-cyber-green text-cyber-green bg-cyber-green/10' : 'border-cyber-neon text-cyber-neon bg-cyber-neon/10'}`}>
                {responseMsg.type === 'success' ? '> STATUS: ACCESSED\n' : '> STATUS: BLOCKED\n'}
                {responseMsg.text}
              </div>
            )}
          </div>
        </div>

        {/* Audit Logs Table */}
        <div className="col-span-1 lg:col-span-2">
          <div className="bg-cyber-card border border-cyber-border/30 rounded-lg p-6 shadow-[0_0_15px_rgba(0,240,255,0.05)] h-full relative overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-cyber-border via-cyber-neon to-cyber-border"></div>
            <h2 className="text-xl font-mono flex items-center justify-between mb-6 text-cyber-border">
              <span className="flex items-center gap-2"><Activity size={20} /> AUDIT LOGS (REAL-TIME)</span>
              <span className="text-xs text-gray-500 flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-cyber-green animate-pulse"></span>
                LIVE SYNC
              </span>
            </h2>

            <div className="overflow-x-auto h-[600px] overflow-y-auto pr-2 custom-scrollbar">
              <table className="w-full text-left font-mono text-sm">
                <thead className="sticky top-0 bg-cyber-card z-10 text-gray-400 border-b border-cyber-border/20">
                  <tr>
                    <th className="pb-3 px-2">ID</th>
                    <th className="pb-3 px-2">TIMESTAMP</th>
                    <th className="pb-3 px-2">IP ADDRESS</th>
                    <th className="pb-3 px-2">STATUS</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-cyber-border/10">
                  {logs.map((log) => (
                    <tr key={log.id} className="hover:bg-white/5 transition-colors">
                      <td className="py-3 px-2 text-gray-500">#{log.id}</td>
                      <td className="py-3 px-2">{new Date(log.timestamp).toLocaleTimeString([], { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit', fractionalSecondDigits: 3 })}</td>
                      <td className="py-3 px-2 text-cyber-border">{log.clientIp}</td>
                      <td className="py-3 px-2">
                        {log.blocked ? (
                          <span className="inline-flex items-center gap-1 text-cyber-neon bg-cyber-neon/10 px-2 py-1 rounded text-xs border border-cyber-neon/30">
                            <ShieldAlert size={12} /> BLOCKED (429)
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 text-cyber-green bg-cyber-green/10 px-2 py-1 rounded text-xs border border-cyber-green/30">
                            <ShieldCheck size={12} /> ALLOWED (200)
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                  {logs.length === 0 && (
                    <tr>
                      <td colSpan="4" className="text-center py-8 text-gray-500 italic">No logs found. Awaiting traffic...</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
