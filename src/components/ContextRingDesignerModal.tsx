import React, { useState } from 'react';
import { ContextRingAction, ContextRingConfig, ContextRingDefinition, RingActionType, AppItem } from '../launcher/types';
import {
  X,
  Plus,
  Trash2,
  Copy,
  RotateCcw,
  Download,
  Upload,
  ArrowUp,
  ArrowDown,
  Check,
  Share2,
  Sliders
} from './Icons';

interface ContextRingDesignerModalProps {
  config: ContextRingConfig;
  allApps: AppItem[];
  onSaveConfig: (updated: ContextRingConfig) => void;
  onClose: () => void;
}

export function ContextRingDesignerModal({
  config,
  allApps,
  onSaveConfig,
  onClose
}: ContextRingDesignerModalProps) {
  const [rings, setRings] = useState<ContextRingDefinition[]>(config.rings);
  const [selectedRingId, setSelectedRingId] = useState<string>(
    config.rings[0]?.id || 'global_default'
  );
  const [appAssignments, setAppAssignments] = useState<Record<string, string>>(config.appAssignments);
  const [workspaceAssignments, setWorkspaceAssignments] = useState<Record<string, string>>(config.workspaceAssignments);
  const [hapticIntensity, setHapticIntensity] = useState(config.hapticIntensity || 'medium');
  const [gestureTrigger, setGestureTrigger] = useState(config.gestureTrigger || 'long_press');

  const [showJsonModal, setShowJsonModal] = useState<boolean>(false);
  const [jsonText, setJsonText] = useState<string>('');
  const [jsonMode, setJsonMode] = useState<'export' | 'import'>('export');

  const activeRing = rings.find((r) => r.id === selectedRingId) || rings[0];

  const updateActiveRing = (fields: Partial<ContextRingDefinition>) => {
    setRings((prev) =>
      prev.map((r) => (r.id === activeRing.id ? { ...r, ...fields } : r))
    );
  };

  const handleCreateRing = () => {
    const newRing: ContextRingDefinition = {
      id: `ring_${Date.now().toString(36)}`,
      title: 'Custom Ring',
      description: 'User radial command ring',
      slotCount: 6,
      radiusDp: 140,
      iconSizeDp: 44,
      isSystemPreset: false,
      actions: [
        { id: `act_${Math.random()}`, title: 'App Drawer', iconName: 'Grid', actionType: 'OPEN_APP_DRAWER', accentHex: '#2A9D8F' },
        { id: `act_${Math.random()}`, title: 'Command Bar', iconName: 'Terminal', actionType: 'OPEN_COMMAND_BAR', accentHex: '#457B9D' },
        { id: `act_${Math.random()}`, title: 'Focus Mode', iconName: 'Shield', actionType: 'TOGGLE_FOCUS_MODE', accentHex: '#E76F51' },
        { id: `act_${Math.random()}`, title: 'Settings', iconName: 'Settings', actionType: 'OPEN_SETTINGS', accentHex: '#6C757D' }
      ]
    };
    setRings((prev) => [...prev, newRing]);
    setSelectedRingId(newRing.id);
  };

  const handleDuplicateRing = () => {
    const copy: ContextRingDefinition = {
      ...activeRing,
      id: `ring_${Date.now().toString(36)}`,
      title: `${activeRing.title} (Copy)`,
      isSystemPreset: false
    };
    setRings((prev) => [...prev, copy]);
    setSelectedRingId(copy.id);
  };

  const handleDeleteRing = () => {
    if (activeRing.id === 'global_default' || rings.length <= 1) return;
    const remaining = rings.filter((r) => r.id !== activeRing.id);
    setRings(remaining);
    setSelectedRingId(remaining[0].id);
  };

  const handleAddAction = () => {
    const newAction: ContextRingAction = {
      id: `act_${Date.now().toString(36)}`,
      title: 'New Action',
      iconName: 'Compass',
      actionType: 'OPEN_COMMAND_BAR',
      accentHex: '#2A9D8F'
    };
    updateActiveRing({ actions: [...activeRing.actions, newAction] });
  };

  const handleMoveAction = (index: number, direction: -1 | 1) => {
    const list = [...activeRing.actions];
    const targetIdx = index + direction;
    if (targetIdx < 0 || targetIdx >= list.length) return;
    const [item] = list.splice(index, 1);
    list.splice(targetIdx, 0, item);
    updateActiveRing({ actions: list });
  };

  const handleRemoveAction = (index: number) => {
    const list = activeRing.actions.filter((_, i) => i !== index);
    updateActiveRing({ actions: list });
  };

  const handleSaveAndClose = () => {
    const updated: ContextRingConfig = {
      defaultGlobalRingId: config.defaultGlobalRingId,
      rings,
      appAssignments,
      workspaceAssignments,
      folderAssignments: config.folderAssignments || {},
      hapticIntensity,
      gestureTrigger,
      showLocalSuggestions: config.showLocalSuggestions
    };
    onSaveConfig(updated);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 overflow-y-auto">
      <div className="bg-[#14171E] border border-neutral-700 w-full max-w-4xl rounded-2xl shadow-2xl flex flex-col max-h-[92vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-neutral-800">
          <div>
            <h3 className="text-white text-lg font-bold">Context Ring Designer</h3>
            <p className="text-[#2A9D8F] text-xs font-medium">Advanced Radial Command System</p>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                setJsonMode('export');
                setJsonText(JSON.stringify({ rings, appAssignments, workspaceAssignments }, null, 2));
                setShowJsonModal(true);
              }}
              className="p-2 rounded-lg bg-neutral-800 hover:bg-neutral-700 text-neutral-300 text-xs flex items-center gap-1.5"
            >
              <Share2 size={14} />
              Export / Import
            </button>
            <button
              onClick={onClose}
              className="p-2 rounded-lg hover:bg-neutral-800 text-neutral-400 hover:text-white"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Ring Tabs */}
        <div className="flex items-center gap-2 px-6 py-3 bg-[#0F1218] border-b border-neutral-800/80 overflow-x-auto">
          {rings.map((r) => (
            <button
              key={r.id}
              onClick={() => setSelectedRingId(r.id)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-all ${
                r.id === activeRing.id
                  ? 'bg-[#2A9D8F] text-white shadow-sm'
                  : 'bg-neutral-800/60 hover:bg-neutral-800 text-neutral-300'
              }`}
            >
              {r.title}
            </button>
          ))}
          <button
            onClick={handleCreateRing}
            className="p-1.5 rounded-lg bg-neutral-800 hover:bg-neutral-700 text-[#2A9D8F] text-xs flex items-center gap-1 font-semibold"
          >
            <Plus size={14} /> New Ring
          </button>
        </div>

        {/* Content Body: Two Column */}
        <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-6 p-6 overflow-y-auto">
          {/* Left: Interactive Radial Preview */}
          <div className="flex flex-col items-center justify-center bg-[#181D26] border border-neutral-800 rounded-xl p-4 relative min-h-[340px]">
            <span className="absolute top-3 left-3 text-[11px] font-bold text-neutral-400 uppercase tracking-wider">
              Live Radial Geometry
            </span>

            {/* SVG Visualizer */}
            <svg className="w-full h-[280px]">
              <circle
                cx="50%"
                cy="50%"
                r={activeRing.radiusDp}
                fill="none"
                stroke="#2A9D8F"
                strokeWidth="1.5"
                strokeDasharray="6 6"
                strokeOpacity="0.4"
              />
              <circle
                cx="50%"
                cy="50%"
                r={activeRing.radiusDp * 0.45}
                fill="none"
                stroke="#2A9D8F"
                strokeWidth="1"
                strokeOpacity="0.25"
              />

              {/* Action Slots */}
              {activeRing.actions.map((act, i) => {
                const total = activeRing.actions.length;
                const angle = (2 * Math.PI * i) / total - Math.PI / 2;
                const cx = 200 + activeRing.radiusDp * Math.cos(angle);
                const cy = 140 + activeRing.radiusDp * Math.sin(angle);

                return (
                  <g key={act.id}>
                    <line
                      x1="200"
                      y1="140"
                      x2={cx}
                      y2={cy}
                      stroke="#2A9D8F"
                      strokeWidth="1"
                      strokeOpacity="0.3"
                    />
                    <circle
                      cx={cx}
                      cy={cy}
                      r={activeRing.iconSizeDp / 2}
                      fill={act.accentHex || '#2A9D8F'}
                      stroke="#FFFFFF"
                      strokeWidth="1.5"
                    />
                    <text
                      x={cx}
                      y={cy + 4}
                      fill="#FFFFFF"
                      fontSize="9"
                      fontWeight="bold"
                      textAnchor="middle"
                    >
                      {i + 1}
                    </text>
                    <text
                      x={cx}
                      y={cy + (activeRing.iconSizeDp / 2 + 12)}
                      fill="#E2E8F0"
                      fontSize="9"
                      fontWeight="500"
                      textAnchor="middle"
                    >
                      {act.title}
                    </text>
                  </g>
                );
              })}

              {/* Center Hub */}
              <circle
                cx="200"
                cy="140"
                r="24"
                fill="#12161F"
                stroke="#2A9D8F"
                strokeWidth="2"
              />
              <text
                x="200"
                y="142"
                fill="#FFFFFF"
                fontSize="8"
                fontWeight="bold"
                textAnchor="middle"
              >
                HUB
              </text>
            </svg>

            {/* Quick action bar */}
            <div className="flex gap-2 mt-2 w-full justify-between items-center border-t border-neutral-800 pt-3">
              <span className="text-neutral-400 text-xs">
                Slots: <strong className="text-white">{activeRing.actions.length} / {activeRing.slotCount}</strong>
              </span>
              <div className="flex gap-2">
                <button
                  onClick={handleDuplicateRing}
                  className="px-2.5 py-1 rounded bg-neutral-800 hover:bg-neutral-700 text-xs text-neutral-300 flex items-center gap-1"
                >
                  <Copy size={12} /> Duplicate
                </button>
                {!activeRing.isSystemPreset && activeRing.id !== 'global_default' && (
                  <button
                    onClick={handleDeleteRing}
                    className="px-2.5 py-1 rounded bg-rose-950/40 hover:bg-rose-900/60 text-xs text-rose-300 flex items-center gap-1"
                  >
                    <Trash2 size={12} /> Delete
                  </button>
                )}
              </div>
            </div>
          </div>

          {/* Right: Ring Configuration & Actions Inspector */}
          <div className="flex flex-col gap-4">
            {/* Title & Description */}
            <div className="space-y-3 bg-[#181D26] border border-neutral-800 rounded-xl p-4">
              <label className="block text-xs font-bold text-neutral-400 uppercase tracking-wider">
                Ring Title & Description
              </label>
              <input
                type="text"
                value={activeRing.title}
                onChange={(e) => updateActiveRing({ title: e.target.value })}
                className="w-full bg-[#12161F] border border-neutral-700 rounded-lg px-3 py-2 text-white text-sm focus:border-[#2A9D8F] focus:outline-hidden"
                placeholder="Ring Title"
              />
              <input
                type="text"
                value={activeRing.description || ''}
                onChange={(e) => updateActiveRing({ description: e.target.value })}
                className="w-full bg-[#12161F] border border-neutral-700 rounded-lg px-3 py-2 text-white text-xs focus:border-[#2A9D8F] focus:outline-hidden"
                placeholder="Purpose / Description"
              />

              {/* Slot count selection */}
              <div>
                <label className="block text-xs font-semibold text-neutral-300 mb-1.5">
                  Action Positions: {activeRing.slotCount}
                </label>
                <div className="grid grid-cols-4 gap-2">
                  {[4, 6, 8, 10].map((count) => (
                    <button
                      key={count}
                      onClick={() => updateActiveRing({ slotCount: count })}
                      className={`py-1.5 rounded-lg text-xs font-bold transition-all ${
                        activeRing.slotCount === count
                          ? 'bg-[#2A9D8F] text-white'
                          : 'bg-neutral-800 text-neutral-400 hover:text-white'
                      }`}
                    >
                      {count}
                    </button>
                  ))}
                </div>
              </div>

              {/* Radius & Icon Size sliders */}
              <div className="grid grid-cols-2 gap-3 pt-1">
                <div>
                  <div className="flex justify-between text-xs text-neutral-300 mb-1">
                    <span>Radius</span>
                    <span className="text-[#2A9D8F] font-bold">{activeRing.radiusDp} dp</span>
                  </div>
                  <input
                    type="range"
                    min="100"
                    max="180"
                    value={activeRing.radiusDp}
                    onChange={(e) => updateActiveRing({ radiusDp: parseInt(e.target.value) })}
                    className="w-full accent-[#2A9D8F]"
                  />
                </div>
                <div>
                  <div className="flex justify-between text-xs text-neutral-300 mb-1">
                    <span>Icon Size</span>
                    <span className="text-[#2A9D8F] font-bold">{activeRing.iconSizeDp} dp</span>
                  </div>
                  <input
                    type="range"
                    min="36"
                    max="52"
                    value={activeRing.iconSizeDp}
                    onChange={(e) => updateActiveRing({ iconSizeDp: parseInt(e.target.value) })}
                    className="w-full accent-[#2A9D8F]"
                  />
                </div>
              </div>
            </div>

            {/* Actions List */}
            <div className="bg-[#181D26] border border-neutral-800 rounded-xl p-4 flex-1 flex flex-col">
              <div className="flex justify-between items-center mb-3">
                <span className="text-xs font-bold text-neutral-400 uppercase tracking-wider">
                  Configured Actions ({activeRing.actions.length})
                </span>
                <button
                  onClick={handleAddAction}
                  className="px-2.5 py-1 rounded bg-[#2A9D8F]/20 text-[#2A9D8F] hover:bg-[#2A9D8F]/30 text-xs font-semibold flex items-center gap-1"
                >
                  <Plus size={12} /> Add
                </button>
              </div>

              <div className="space-y-2 overflow-y-auto max-h-[220px] pr-1">
                {activeRing.actions.map((act, idx) => (
                  <div
                    key={act.id}
                    className="flex items-center justify-between p-2.5 bg-[#12161F] border border-neutral-800 rounded-lg text-xs"
                  >
                    <div className="flex items-center gap-2 flex-1 mr-2">
                      <span className="w-5 h-5 rounded-full bg-[#2A9D8F] text-white flex items-center justify-center text-[10px] font-bold">
                        {idx + 1}
                      </span>
                      <input
                        type="text"
                        value={act.title}
                        onChange={(e) => {
                          const updated = [...activeRing.actions];
                          updated[idx] = { ...act, title: e.target.value };
                          updateActiveRing({ actions: updated });
                        }}
                        className="bg-transparent text-white font-medium focus:outline-hidden w-28"
                      />
                      <span className="text-[10px] text-neutral-500 uppercase">{act.actionType}</span>
                    </div>

                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => handleMoveAction(idx, -1)}
                        disabled={idx === 0}
                        className="p-1 rounded text-neutral-400 hover:text-white disabled:opacity-20"
                      >
                        <ArrowUp size={12} />
                      </button>
                      <button
                        onClick={() => handleMoveAction(idx, 1)}
                        disabled={idx === activeRing.actions.length - 1}
                        className="p-1 rounded text-neutral-400 hover:text-white disabled:opacity-20"
                      >
                        <ArrowDown size={12} />
                      </button>
                      <button
                        onClick={() => handleRemoveAction(idx)}
                        className="p-1 rounded text-rose-400 hover:text-rose-300"
                      >
                        <Trash2 size={12} />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Context Bindings */}
            <div className="bg-[#181D26] border border-neutral-800 rounded-xl p-4">
              <span className="text-xs font-bold text-neutral-400 uppercase tracking-wider block mb-2">
                Context Bindings (Priority Hierarchy)
              </span>
              <div className="grid grid-cols-2 gap-2 text-xs">
                <div>
                  <span className="text-neutral-400 block mb-1">Bind to Workspace:</span>
                  <select
                    value={
                      Object.entries(workspaceAssignments).find(([_, rId]) => rId === activeRing.id)?.[0] || 'none'
                    }
                    onChange={(e) => {
                      const ws = e.target.value;
                      const copy = { ...workspaceAssignments };
                      if (ws === 'none') {
                        Object.keys(copy).forEach((k) => {
                          if (copy[k] === activeRing.id) delete copy[k];
                        });
                      } else {
                        copy[ws] = activeRing.id;
                      }
                      setWorkspaceAssignments(copy);
                    }}
                    className="w-full bg-[#12161F] border border-neutral-700 rounded p-1.5 text-white"
                  >
                    <option value="none">None (Use Global)</option>
                    <option value="personal">Personal Workspace</option>
                    <option value="work">Work Workspace</option>
                    <option value="study">Study Workspace</option>
                    <option value="gaming">Gaming Workspace</option>
                    <option value="travel">Travel Workspace</option>
                  </select>
                </div>

                <div>
                  <span className="text-neutral-400 block mb-1">Bind to App:</span>
                  <select
                    value={
                      Object.entries(appAssignments).find(([_, rId]) => rId === activeRing.id)?.[0] || 'none'
                    }
                    onChange={(e) => {
                      const pkg = e.target.value;
                      const copy = { ...appAssignments };
                      if (pkg === 'none') {
                        Object.keys(copy).forEach((k) => {
                          if (copy[k] === activeRing.id) delete copy[k];
                        });
                      } else {
                        copy[pkg] = activeRing.id;
                      }
                      setAppAssignments(copy);
                    }}
                    className="w-full bg-[#12161F] border border-neutral-700 rounded p-1.5 text-white"
                  >
                    <option value="none">None</option>
                    <option value="com.brave.browser">Brave Browser</option>
                    <option value="com.android.chrome">Chrome</option>
                    <option value="com.spotify.music">Spotify Music</option>
                    <option value="com.discord">Discord</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between px-6 py-4 bg-[#0F1218] border-t border-neutral-800">
          <div className="flex items-center gap-2">
            <span className="text-xs text-neutral-400">Trigger:</span>
            <select
              value={gestureTrigger}
              onChange={(e) => setGestureTrigger(e.target.value as any)}
              className="bg-neutral-800 text-xs text-neutral-200 border border-neutral-700 rounded px-2 py-1"
            >
              <option value="long_press">Long Press</option>
              <option value="double_tap">Double Tap</option>
              <option value="swipe_up">Swipe Up</option>
            </select>
          </div>

          <div className="flex gap-2">
            <button
              onClick={onClose}
              className="px-4 py-2 rounded-lg border border-neutral-700 hover:bg-neutral-800 text-neutral-300 text-xs font-semibold"
            >
              Cancel
            </button>
            <button
              onClick={handleSaveAndClose}
              className="px-5 py-2 rounded-lg bg-[#2A9D8F] hover:bg-[#2A9D8F]/90 text-white text-xs font-semibold flex items-center gap-1.5 shadow-md"
            >
              <Check size={14} /> Save Configuration
            </button>
          </div>
        </div>
      </div>

      {/* Export / Import Modal */}
      {showJsonModal && (
        <div className="fixed inset-0 z-60 flex items-center justify-center bg-black/80 p-4">
          <div className="bg-[#181D26] border border-neutral-700 rounded-xl p-5 max-w-lg w-full">
            <h4 className="text-white text-base font-bold mb-2">
              {jsonMode === 'export' ? 'Export Context Rings JSON' : 'Import Context Rings JSON'}
            </h4>
            <textarea
              value={jsonText}
              onChange={(e) => setJsonText(e.target.value)}
              rows={12}
              className="w-full bg-[#12161F] border border-neutral-700 rounded-lg p-3 text-xs font-mono text-neutral-200 focus:outline-hidden"
            />
            <div className="flex justify-end gap-2 mt-4">
              <button
                onClick={() => setShowJsonModal(false)}
                className="px-3 py-1.5 rounded border border-neutral-600 text-neutral-300 text-xs"
              >
                Close
              </button>
              {jsonMode === 'import' && (
                <button
                  onClick={() => {
                    try {
                      const parsed = JSON.parse(jsonText);
                      if (parsed.rings) setRings(parsed.rings);
                      if (parsed.appAssignments) setAppAssignments(parsed.appAssignments);
                      if (parsed.workspaceAssignments) setWorkspaceAssignments(parsed.workspaceAssignments);
                      setShowJsonModal(false);
                    } catch (_e) {
                      alert('Invalid JSON format');
                    }
                  }}
                  className="px-3 py-1.5 rounded bg-[#2A9D8F] text-white text-xs font-semibold"
                >
                  Apply Import
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
