import React from 'react';
import {
  ShoppingBag,
  Store,
  ShieldCheck,
  Sparkles,
  Gamepad2,
  Globe,
  Calculator,
  Calendar,
  Camera,
  Bot,
  Compass,
  Clock,
  Navigation,
  Users,
  MessagesSquare,
  HardDrive,
  FileText,
  Flame,
  Settings,
  Headphones,
  MessageSquare,
  PlaySquare,
  Send,
  Lock,
  Search,
  ChevronRight,
  ArrowLeft,
  Plus,
  Play,
  Pause,
  SkipForward,
  SkipBack,
  Check,
  Moon,
  Sun,
  Palette,
  Smartphone,
  Eye,
  EyeOff,
  Download,
  Code2,
  Sliders,
  Grid,
  Maximize2,
  Minimize2,
  Pin,
  Trash2,
  FolderPlus,
  Shield,
  Layers,
  Wifi,
  PlayCircle,
  Music,
  Bluetooth,
  Hash,
  Edit3,
  ArrowUp,
  ArrowDown,
  Folder,
  MessageCircle,
  Battery,
  Zap,
  AlertTriangle,
  Copy,
  RotateCcw,
  Share2,
  Upload,
  X
} from 'lucide-react';
import { IconShape, IconStyle, LauncherTheme } from '../launcher/types';

interface AppIconProps {
  name: string;
  color: string;
  shape: IconShape;
  style: IconStyle;
  theme: LauncherTheme;
  accentColor?: string;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export const AppIcon: React.FC<AppIconProps> = ({
  name,
  color,
  shape,
  style,
  theme,
  accentColor = '#2A9D8F',
  size = 'md',
  className = ''
}) => {
  const getIconElement = () => {
    const iconProps = {
      size: size === 'sm' ? 14 : size === 'lg' ? 26 : 18,
      strokeWidth: 2
    };

    switch (name) {
      case 'ShoppingBag':
        return <ShoppingBag {...iconProps} />;
      case 'Store':
        return <Store {...iconProps} />;
      case 'ShieldCheck':
        return <ShieldCheck {...iconProps} />;
      case 'Sparkles':
        return <Sparkles {...iconProps} />;
      case 'Gamepad2':
        return <Gamepad2 {...iconProps} />;
      case 'Globe':
        return <Globe {...iconProps} />;
      case 'Calculator':
        return <Calculator {...iconProps} />;
      case 'Calendar':
        return <Calendar {...iconProps} />;
      case 'Camera':
        return <Camera {...iconProps} />;
      case 'Bot':
        return <Bot {...iconProps} />;
      case 'Compass':
        return <Compass {...iconProps} />;
      case 'Clock':
        return <Clock {...iconProps} />;
      case 'Navigation':
        return <Navigation {...iconProps} />;
      case 'Users':
        return <Users {...iconProps} />;
      case 'MessagesSquare':
        return <MessagesSquare {...iconProps} />;
      case 'HardDrive':
        return <HardDrive {...iconProps} />;
      case 'FileText':
        return <FileText {...iconProps} />;
      case 'Flame':
        return <Flame {...iconProps} />;
      case 'Settings':
        return <Settings {...iconProps} />;
      case 'Headphones':
        return <Headphones {...iconProps} />;
      case 'MessageSquare':
        return <MessageSquare {...iconProps} />;
      case 'PlaySquare':
        return <PlaySquare {...iconProps} />;
      case 'Send':
        return <Send {...iconProps} />;
      case 'Twitter':
        return (
          <span className="font-bold font-sans text-xs tracking-tighter" style={{ fontSize: size === 'sm' ? 10 : 15 }}>
            𝕏
          </span>
        );
      case 'Lock':
        return <Lock {...iconProps} />;
      default:
        return <Globe {...iconProps} />;
    }
  };

  // Shape class
  let shapeClass = 'rounded-2xl';
  switch (shape) {
    case 'circle':
      shapeClass = 'rounded-full';
      break;
    case 'squircle':
      shapeClass = 'rounded-[16px]';
      break;
    case 'rounded-square':
      shapeClass = 'rounded-xl';
      break;
    case 'square':
      shapeClass = 'rounded-md';
      break;
    case 'pebble':
      shapeClass = 'rounded-tl-2xl rounded-br-2xl rounded-tr-lg rounded-bl-lg';
      break;
  }

  // Size class
  const sizeClass =
    size === 'sm' ? 'w-7 h-7' : size === 'lg' ? 'w-13 h-13' : 'w-10 h-10';

  // Style class
  let backgroundStyle: React.CSSProperties = { backgroundColor: color };
  let contentColorClass = 'text-white';

  if (style === 'monochrome') {
    backgroundStyle = {
      backgroundColor: theme === 'warm-light' ? '#3B403D' : '#2A2E33'
    };
    contentColorClass = theme === 'warm-light' ? 'text-[#F5F4F0]' : 'text-[#E2E8F0]';
  } else if (style === 'outline') {
    backgroundStyle = {
      backgroundColor: 'transparent',
      borderColor: theme === 'warm-light' ? '#3B403D40' : 'rgba(255,255,255,0.25)',
      borderWidth: 1.5
    };
    contentColorClass = theme === 'warm-light' ? 'text-[#2D312E]' : 'text-[#F1F3F4]';
  } else if (style === 'custom') {
    backgroundStyle = { backgroundColor: accentColor };
    contentColorClass = 'text-white';
  }

  return (
    <div
      style={backgroundStyle}
      className={`inline-flex items-center justify-center shrink-0 transition-all duration-200 select-none ${shapeClass} ${sizeClass} ${contentColorClass} ${className}`}
    >
      {getIconElement()}
    </div>
  );
};

export {
  Search,
  ChevronRight,
  ArrowLeft,
  ArrowUp,
  ArrowDown,
  Plus,
  Play,
  PlayCircle,
  Pause,
  SkipForward,
  SkipBack,
  Check,
  Moon,
  Sun,
  Palette,
  Smartphone,
  Eye,
  EyeOff,
  Download,
  Upload,
  Code2,
  Sliders,
  Grid,
  Maximize2,
  Minimize2,
  Pin,
  Trash2,
  Folder,
  FolderPlus,
  Lock,
  Settings,
  Shield,
  Layers,
  Compass,
  Globe,
  Wifi,
  Music,
  Bluetooth,
  Hash,
  Edit3,
  MessageCircle,
  Battery,
  Zap,
  AlertTriangle,
  Copy,
  RotateCcw,
  Share2,
  Sparkles,
  X
};
