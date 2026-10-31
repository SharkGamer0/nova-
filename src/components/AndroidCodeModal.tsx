import React, { useState } from 'react';
import JSZip from 'jszip';
import { ANDROID_PROJECT_FILES } from '../launcher/androidProjectFiles';
import { Download, Code2, Check, ArrowLeft, Smartphone } from './Icons';

interface AndroidCodeModalProps {
  onClose: () => void;
  accentColor: string;
}

export const AndroidCodeModal: React.FC<AndroidCodeModalProps> = ({ onClose, accentColor }) => {
  const [selectedFilePath, setSelectedFilePath] = useState<string>('app/src/main/AndroidManifest.xml');
  const [copied, setCopied] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);

  const currentFile = ANDROID_PROJECT_FILES.find((f) => f.path === selectedFilePath) || ANDROID_PROJECT_FILES[0];

  const handleCopy = () => {
    navigator.clipboard.writeText(currentFile.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadZip = async () => {
    setIsDownloading(true);
    try {
      const zip = new JSZip();

      // Add all project files
      for (const file of ANDROID_PROJECT_FILES) {
        if (file.isBinary && file.binaryBase64) {
          zip.file(file.path, file.binaryBase64, { base64: true });
        } else {
          const isExecutable = file.path === 'gradlew';
          zip.file(file.path, file.content, isExecutable ? { unixPermissions: '755' } : {});
        }
      }

      const content = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(content);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'NovaForge-Launcher-AndroidStudio.zip';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Failed to create ZIP', err);
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-md flex items-center justify-center p-4 sm:p-6 animate-in fade-in">
      <div className="w-full max-w-5xl h-[88vh] bg-[#121417] text-[#E2E8F0] border border-white/10 rounded-3xl shadow-2xl flex flex-col overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/10 bg-[#16191D]">
          <div className="flex items-center gap-3">
            <div
              style={{ backgroundColor: accentColor }}
              className="w-9 h-9 rounded-xl flex items-center justify-center text-white"
            >
              <Smartphone size={20} />
            </div>
            <div>
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                NovaForge Android Studio Project
                <span className="text-[10px] bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 px-2 py-0.5 rounded-full font-mono">
                  Kotlin + Compose + Material 3
                </span>
              </h2>
              <p className="text-xs text-neutral-400">
                Genuine Android HOME launcher with Native AppWidgetHost, PackageManager & DataStore
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={handleDownloadZip}
              disabled={isDownloading}
              style={{ backgroundColor: accentColor }}
              className="text-xs font-semibold px-4 py-2 rounded-xl text-white flex items-center gap-2 shadow-md hover:scale-102 active:scale-98 transition disabled:opacity-50"
            >
              <Download size={15} />
              {isDownloading ? 'Packaging...' : 'Download Project (.ZIP)'}
            </button>
            <button
              onClick={onClose}
              className="p-2 rounded-xl bg-white/5 hover:bg-white/10 text-neutral-400 hover:text-white transition text-xs"
            >
              ✕ Close
            </button>
          </div>
        </div>

        {/* Content body: File tree left, code viewer right */}
        <div className="flex-1 flex overflow-hidden">
          {/* File Explorer Tree */}
          <div className="w-72 border-r border-white/10 bg-[#0E1013] overflow-y-auto p-4 space-y-1 text-xs">
            <div className="text-[11px] font-semibold text-neutral-400 uppercase tracking-wider mb-2 px-2">
              Project Structure
            </div>
            {ANDROID_PROJECT_FILES.map((file) => {
              const isSelected = selectedFilePath === file.path;
              return (
                <button
                  key={file.path}
                  onClick={() => setSelectedFilePath(file.path)}
                  className={`w-full text-left px-3 py-2 rounded-xl transition flex flex-col gap-0.5 ${
                    isSelected
                      ? 'bg-white/10 text-white font-medium border border-white/15'
                      : 'text-neutral-400 hover:text-neutral-200 hover:bg-white/5'
                  }`}
                >
                  <span className="font-mono text-xs truncate">{file.path}</span>
                  <span className="text-[10px] text-neutral-400 truncate">{file.description}</span>
                </button>
              );
            })}

            {/* CLI Build Command Hint */}
            <div className="mt-6 p-3 rounded-2xl bg-white/5 border border-white/10 text-[11px] space-y-2">
              <div className="font-semibold text-neutral-300">Terminal Build:</div>
              <code className="block bg-black/60 p-2 rounded font-mono text-[10px] text-emerald-400 select-all">
                ./gradlew assembleDebug
              </code>
              <div className="text-neutral-400 text-[10px]">
                Produces installable APK at <code>app/build/outputs/apk/debug/app-debug.apk</code>
              </div>
            </div>
          </div>

          {/* Code Viewer Panel */}
          <div className="flex-1 flex flex-col bg-[#14171B] overflow-hidden">
            {/* File info bar */}
            <div className="flex items-center justify-between px-6 py-2.5 border-b border-white/10 bg-[#161A1F] text-xs">
              <span className="font-mono text-neutral-300">{currentFile.path}</span>
              <button
                onClick={handleCopy}
                className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-neutral-300 hover:text-white transition text-xs"
              >
                {copied ? <Check size={14} className="text-emerald-400" /> : <Code2 size={14} />}
                <span>{copied ? 'Copied!' : 'Copy Code'}</span>
              </button>
            </div>

            {/* Code editor / pre */}
            <div className="flex-1 overflow-auto p-6 font-mono text-xs leading-relaxed text-neutral-200 bg-[#0D0F12]">
              <pre>
                <code>{currentFile.content}</code>
              </pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
