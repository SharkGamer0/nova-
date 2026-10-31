import React, { useState, useRef, useEffect } from 'react';
import { ActiveRingSession, ContextRingAction, ContextRingConfig } from '../launcher/types';
import { evaluatePointerAngleAndSlot, calculateClampedCenter } from '../launcher/logic';
import {
  Grid,
  Search,
  Settings,
  Shield,
  Layers,
  Compass,
  Globe,
  Wifi,
  Sun,
  Play,
  PlayCircle,
  SkipForward,
  SkipBack,
  Music,
  Bluetooth,
  Hash,
  Edit3,
  ArrowLeft,
  Folder,
  MessageCircle,
  Battery,
  Zap,
  Check,
  X,
  AlertTriangle
} from './Icons';

interface ContextRingOverlayProps {
  session: ActiveRingSession;
  config: ContextRingConfig;
  onSelectAction: (action: ContextRingAction) => void;
  onNavigateBack: () => void;
  onDismiss: () => void;
  onPushNestedRing: (targetId: string) => void;
}

export function ContextRingOverlay({
  session,
  config,
  onSelectAction,
  onNavigateBack,
  onDismiss,
  onPushNestedRing
}: ContextRingOverlayProps) {
  const currentRing = session.currentRing;
  const actions = currentRing.actions;
  const radius = currentRing.radiusDp || 135;
  const iconSize = currentRing.iconSizeDp || 44;

  const [clampedCenter, setClampedCenter] = useState<[number, number]>([session.originX, session.originY]);
  const [highlightedIndex, setHighlightedIndex] = useState<number>(session.selectedIndex ?? -1);
  const [pendingMacroConfirm, setPendingMacroConfirm] = useState<ContextRingAction | null>(null);

  const containerRef = useRef<HTMLDivElement>(null);

  // Recalculate clamped center based on container bounds
  useEffect(() => {
    if (containerRef.current) {
      const rect = containerRef.current.getBoundingClientRect();
      const [cx, cy] = calculateClampedCenter(
        session.originX,
        session.originY,
        rect.width,
        rect.height,
        radius,
        28
      );
      setClampedCenter([cx, cy]);
    }
  }, [session.originX, session.originY, radius]);

  const [centerX, centerY] = clampedCenter;

  const handlePointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!containerRef.current) return;
    const rect = containerRef.current.getBoundingClientRect();
    const px = e.clientX - rect.left;
    const py = e.clientY - rect.top;

    const { selectedIndex } = evaluatePointerAngleAndSlot(
      px,
      py,
      centerX,
      centerY,
      actions.length,
      28
    );
    setHighlightedIndex(selectedIndex);
  };

  const handlePointerUp = () => {
    if (highlightedIndex >= 0 && highlightedIndex < actions.length) {
      const act = actions[highlightedIndex];
      handleActionClick(act);
    }
  };

  const handleActionClick = (action: ContextRingAction) => {
    if (action.actionType === 'NESTED_RING' && action.payload) {
      onPushNestedRing(action.payload);
      return;
    }
    if (action.requiresConfirmation) {
      setPendingMacroConfirm(action);
      return;
    }
    onSelectAction(action);
  };

  const renderActionIcon = (iconName: string) => {
    switch (iconName.toLowerCase()) {
      case 'grid': return <Grid size={20} />;
      case 'search': return <Search size={20} />;
      case 'terminal': return <Play size={20} />;
      case 'settings': return <Settings size={20} />;
      case 'shield': return <Shield size={20} />;
      case 'layers': return <Layers size={20} />;
      case 'compass': return <Compass size={20} />;
      case 'globe': return <Globe size={20} />;
      case 'wifi': return <Wifi size={20} />;
      case 'sun': return <Sun size={20} />;
      case 'play': return <Play size={20} />;
      case 'playcircle': return <PlayCircle size={20} />;
      case 'skipforward': return <SkipForward size={20} />;
      case 'skipback': return <SkipBack size={20} />;
      case 'music': return <Music size={20} />;
      case 'bluetooth': return <Bluetooth size={20} />;
      case 'hash': return <Hash size={20} />;
      case 'edit3': return <Edit3 size={20} />;
      case 'arrowleft': return <ArrowLeft size={20} />;
      case 'folder': return <Folder size={20} />;
      case 'messagecircle': return <MessageCircle size={20} />;
      case 'battery': return <Battery size={20} />;
      case 'zap': return <Zap size={20} />;
      default: return <Compass size={20} />;
    }
  };

  return (
    <div
      ref={containerRef}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      className="absolute inset-0 z-50 overflow-hidden bg-black/60 backdrop-blur-xs select-none animate-fadeIn"
      onClick={(e) => {
        // If clicking directly on backdrop
        if (e.target === containerRef.current) {
          onDismiss();
        }
      }}
    >
      {/* SVG Radial guideline tracks */}
      <svg className="absolute inset-0 w-full h-full pointer-events-none">
        <circle
          cx={centerX}
          cy={centerY}
          r={radius}
          fill="none"
          stroke="#2A9D8F"
          strokeWidth="1.5"
          strokeDasharray="6 6"
          strokeOpacity="0.4"
        />
        <circle
          cx={centerX}
          cy={centerY}
          r={radius * 0.45}
          fill="none"
          stroke="#2A9D8F"
          strokeWidth="1"
          strokeOpacity="0.25"
        />
        {highlightedIndex >= 0 && highlightedIndex < actions.length && (
          (() => {
            const angleStep = (2 * Math.PI) / actions.length;
            const angle = highlightedIndex * angleStep - Math.PI / 2;
            const sx = centerX + radius * Math.cos(angle);
            const sy = centerY + radius * Math.sin(angle);
            return (
              <line
                x1={centerX}
                y1={centerY}
                x2={sx}
                y2={sy}
                stroke="#2A9D8F"
                strokeWidth="2.5"
                strokeOpacity="0.8"
              />
            );
          })()
        )}
      </svg>

      {/* Action Nodes */}
      {actions.map((action, idx) => {
        const isSelected = highlightedIndex === idx;
        const angleStep = (2 * Math.PI) / actions.length;
        const angle = idx * angleStep - Math.PI / 2;
        const x = centerX + radius * Math.cos(angle);
        const y = centerY + radius * Math.sin(angle);

        return (
          <div
            key={action.id}
            style={{
              left: `${x}px`,
              top: `${y}px`,
              transform: `translate(-50%, -50%) scale(${isSelected ? 1.25 : 1})`
            }}
            onClick={(e) => {
              e.stopPropagation();
              handleActionClick(action);
            }}
            className={`absolute flex flex-col items-center justify-center transition-all duration-150 cursor-pointer ${
              isSelected ? 'z-20' : 'z-10'
            }`}
          >
            <div
              style={{
                width: `${iconSize}px`,
                height: `${iconSize}px`,
                backgroundColor: isSelected ? (action.accentHex || '#2A9D8F') : '#1E232D',
                borderColor: isSelected ? '#FFFFFF' : '#2A9D8F55'
              }}
              className="rounded-full flex items-center justify-center text-white border-2 shadow-lg transition-transform hover:scale-110 active:scale-95"
            >
              {renderActionIcon(action.iconName)}
            </div>

            {/* Label badge */}
            <span
              className={`mt-1.5 px-2 py-0.5 rounded-full text-[11px] font-medium tracking-wide whitespace-nowrap transition-opacity ${
                isSelected
                  ? 'bg-neutral-900 text-white opacity-100 shadow-md ring-1 ring-white/20'
                  : 'bg-neutral-900/80 text-neutral-300 opacity-80'
              }`}
            >
              {action.title}
            </span>
          </div>
        );
      })}

      {/* Center Hub */}
      <div
        style={{
          left: `${centerX}px`,
          top: `${centerY}px`,
          transform: 'translate(-50%, -50%)'
        }}
        onClick={(e) => {
          e.stopPropagation();
          if (session.breadcrumbRingIds.length > 0) {
            onNavigateBack();
          } else {
            onDismiss();
          }
        }}
        className="absolute z-20 w-14 h-14 rounded-full bg-neutral-900 border-2 border-[#2A9D8F] shadow-xl flex flex-col items-center justify-center text-center cursor-pointer hover:border-white transition-all active:scale-95"
      >
        {session.breadcrumbRingIds.length > 0 ? (
          <>
            <ArrowLeft size={16} className="text-[#2A9D8F]" />
            <span className="text-[9px] font-semibold text-neutral-300 uppercase tracking-wider">Back</span>
          </>
        ) : (
          <>
            <span className="text-[10px] font-bold text-neutral-100 leading-tight truncate max-w-[48px]">
              {currentRing.title}
            </span>
            <span className="text-[8px] text-[#2A9D8F] font-medium">
              {actions.length} acts
            </span>
          </>
        )}
      </div>

      {/* Macro confirmation modal */}
      {pendingMacroConfirm && (
        <div
          className="absolute inset-0 z-50 flex items-center justify-center bg-black/75 p-4"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="bg-[#181D26] border border-neutral-700 rounded-2xl p-5 max-w-xs w-full text-center shadow-2xl">
            <div className="w-12 h-12 rounded-full bg-[#2A9D8F]/20 text-[#2A9D8F] mx-auto flex items-center justify-center mb-3">
              <AlertTriangle size={24} />
            </div>
            <h4 className="text-white text-base font-bold mb-1">
              Execute {pendingMacroConfirm.title}?
            </h4>
            <p className="text-neutral-400 text-xs mb-5">
              This action triggers command macro [{pendingMacroConfirm.payload}] with multi-step launcher changes.
            </p>
            <div className="flex gap-2">
              <button
                onClick={() => setPendingMacroConfirm(null)}
                className="flex-1 py-2 px-3 rounded-lg border border-neutral-600 text-neutral-300 text-xs font-semibold hover:bg-neutral-800"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  const act = pendingMacroConfirm;
                  setPendingMacroConfirm(null);
                  onSelectAction(act);
                }}
                className="flex-1 py-2 px-3 rounded-lg bg-[#2A9D8F] text-white text-xs font-semibold hover:bg-[#2A9D8F]/80 shadow-md"
              >
                Execute
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
