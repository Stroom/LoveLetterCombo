/* SystemJS module definition */
declare var module: NodeModule;
declare module 'sockjs-client';
declare module 'stompjs';

interface NodeModule {
  id: string;
}
