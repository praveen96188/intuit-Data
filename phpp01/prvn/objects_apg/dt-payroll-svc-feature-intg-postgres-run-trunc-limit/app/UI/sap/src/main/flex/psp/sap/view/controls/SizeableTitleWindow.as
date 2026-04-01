package psp.sap.view.controls
{
////////////////////////////////////////////////////////////////////////////////
//
//  Copyright (C) 2003-2006 Adobe Macromedia Software LLC and its licensors.
//  All Rights Reserved. The following is Source Code and is subject to all
//  restrictions on such code as contained in the End User License Agreement
//  accompanying this product. If you have received this file from a source
//  other than Adobe, then your use, modification, or distribution of this file
//  requires the prior written permission of Adobe.
//
//  @author Dean Harmon
////////////////////////////////////////////////////////////////////////////////

    import flash.events.Event;
    import flash.events.MouseEvent;
    import flash.geom.Rectangle;
    
    import mx.containers.TitleWindow;
    import mx.controls.Button;
    import mx.controls.scrollClasses.ScrollBar;
    import mx.managers.CursorManager;
    
    import psp.sap.application.SAP;
    
    [Event("startResize")]
    
    [Event("stopResize")]
    
    public class SizeableTitleWindow extends TitleWindow
    {
            // this is a copy of the const from panel.as (which is now private)
            // to make it compile in the new versions of the framework until they fix their bug
            // to make getHeaderHeight protected
            protected static const HEADER_PADDING:Number = 14;
            
            private const dragThreshold:int =12;
            // sanity constraints.  
            private const minSizeWidth:int = 100;  
            private const minSizeHeight:int = 100;
            
            public const cursorSizeNone:int = -1;
            public const cursorSizeNE:int   = 0;
            public const cursorSizeN:int    = 1;
            public const cursorSizeNW:int   = 2;
            public const cursorSizeW:int    = 3;
            public const cursorSizeSW:int   = 4;
            public const cursorSizeS:int    = 5;
            public const cursorSizeSE:int   = 6;
            public const cursorSizeE:int    = 7;
            public const cursorSizeAll:int  = 8;                        

            private var downX:int;
            private var downY:int;
            private var startLeft:int;
            private var startTop:int;
            private var startHeight:int;
            private var startWidth:int;
            
            private var initHeight:int;
            
            
            private var resizeCursor:int;
            private var currentCursorID:int;
            private var prevCursor:int;
            private var isResizing:Boolean;
            
            private var mHorizontalResizeEnabled:Boolean = true;
            private var mVerticalResizeEnabled:Boolean = true;
            
            public var resizeToContent:Boolean = false;
            
            public function SizeableTitleWindow()
            {
                super();
                isResizing = false;
                currentCursorID = CursorManager.NO_CURSOR;
                prevCursor = cursorSizeNone;
            }
            
            
            public function get horizontalResizeEnabled():Boolean {
            	return mHorizontalResizeEnabled;
            }
            
            public function set horizontalResizeEnabled(value:Boolean):void {
            	mHorizontalResizeEnabled = value;
            }
            
            public function get verticalResizeEnabled():Boolean {
            	return mVerticalResizeEnabled;
            }
            
            
            public function set verticalResizeEnabled(value:Boolean):void {
            	mVerticalResizeEnabled = value;
            }
            
            override protected function createChildren():void
            {
                super.createChildren();

                // make the cursor change to the resize cursor
                this.titleBar.addEventListener(MouseEvent.MOUSE_MOVE, titleBar_resizeMoveListener, false, 0, true);
                this.addEventListener(MouseEvent.MOUSE_MOVE, resizeMoveListener, false, 0, true);
                
                this.addEventListener(MouseEvent.MOUSE_OUT, cursorMouseOutListener, false, 0, true);
                
                // since the titlebar mousedown listener calls startDragging, but the listener is private
                // we will do our checking in the overridden startDragging event for the titlebar
                this.addEventListener(MouseEvent.MOUSE_DOWN, resizeDownListener, false, 0, true);

            }           
            
            
            protected function getCursorStyle(x:int, y:int, isTitleBar:Boolean):int
            {
                if (isResizing)
                    return resizeCursor;
                
                // the NW corner has to be done in a seperate section because this 
                // corner is twitchy and we add a 1 pix buffer
                if (x >= 0 && x <= dragThreshold + 1 && y >= 0 && y <= dragThreshold + 1)
                {
                	if(!(verticalResizeEnabled && horizontalResizeEnabled)) return cursorSizeNone;
                    return cursorSizeNW;
                } else if (x >= 0 && x <= dragThreshold)
                {
                    if (y >= this.height - dragThreshold)
                    {
                    	if(!(verticalResizeEnabled && horizontalResizeEnabled)) return cursorSizeNone;
                        return cursorSizeSW;
                    } else {
                    	if(!(horizontalResizeEnabled)) return cursorSizeNone;
                        return cursorSizeW;
                    }
                } else if (x >= this.width - dragThreshold -this.getStyle("borderThicknessRight"))
                {
                    if (y >= 0 && y <= dragThreshold)
                    {
                    	if(!(verticalResizeEnabled && horizontalResizeEnabled)) return cursorSizeNone;
                        return cursorSizeNE;
                    } else if (y >= this.height - dragThreshold)
                    {
                    	if(!(verticalResizeEnabled && horizontalResizeEnabled)) return cursorSizeNone;
                        return cursorSizeSE;
                    } else {
                    	if(!(horizontalResizeEnabled)) return cursorSizeNone;
                        return cursorSizeE;
                    }                    
                } else if (y >= 0 && y <= dragThreshold)
                {
                	if(!(verticalResizeEnabled)) return cursorSizeNone;
                    return cursorSizeN;
                } else if (y >= this.height - dragThreshold)
                {
                	if(!(verticalResizeEnabled)) return cursorSizeNone;
                    return cursorSizeS;
// if you want to have the "move" style cursor when over the title bar, uncomment the next three lines                    
                } else if (isTitleBar)
                {
                    return cursorSizeAll;
                }
                return cursorSizeNone;
            }
            
            protected function clearCursor():void
            {
                if (currentCursorID != CursorManager.NO_CURSOR)
                {
                    CursorManager.removeCursor(currentCursorID);
                    currentCursorID = CursorManager.NO_CURSOR;
                }
                prevCursor = cursorSizeNone;                
            }
            
            /**
             *  @protected
             *  Returns the height of the header.
             */
            override protected function getHeaderHeight():Number
            {
                var headerHeight:Number = getStyle("headerHeight");
                
                if (isNaN(headerHeight))
                    headerHeight = measureHeaderText().height +SizeableTitleWindow.HEADER_PADDING;
                
                return headerHeight;
            }
            
            /**
             *  @protected. Returns a Rectangle containing the largest piece of header
             *  text (can be either the title or status, whichever is bigger).
             */
            protected function measureHeaderText():Rectangle
            {
                var textWidth:Number = 20;
                var textHeight:Number = 14;
                
                if (titleTextField && titleTextField.text)
                {
                    titleTextField.validateNow();
                    textWidth = titleTextField.textWidth;
                    textHeight = titleTextField.textHeight;
                }
                
                if (statusTextField)
                {
                    statusTextField.validateNow();
                    textWidth = Math.max(textWidth, statusTextField.textWidth);
                    textHeight = Math.max(textHeight, statusTextField.textHeight);
                }
                
                return new Rectangle(0, 0, textWidth, textHeight);
            }
                                    
            protected function adjustCursor(event:MouseEvent, isTitleBar:Boolean):void
            {
                var c:int;
                
                // we only want the move event from the title bar itself, not from it&apos;s children
                // otherwise you get weird cursor behavior in the middle of the titlebar
                if (isTitleBar && event.target != titleBar)
                {
                    c = cursorSizeAll;
                } else {
                    c = getCursorStyle(event.currentTarget.mouseX,
                         event.currentTarget.mouseY, isTitleBar);
                }
                
                // don&apos;t switch stuff around if we don&apos;t have to
                if (c == prevCursor)
                {
                    return;
                }
                
                prevCursor = c;
                                
                clearCursor();
                
                switch (c) 
                {
// if you want to have the "move" style cursor when over the title bar, uncomment the next three lines                    
//                    case cursorSizeAll:
//                        currentCursorID = CursorManager.setCursor(sizeAllCursorSymbol, 2, -10, -10);    
//                        break;
                    case cursorSizeE:
                    case cursorSizeW:
                        currentCursorID = CursorManager.setCursor(SAP.instance.icons.sizeWECursorSymbol, 2, -10, -11);     
                        break;
                    case cursorSizeNW:
                    case cursorSizeSE:
                        currentCursorID = CursorManager.setCursor(SAP.instance.icons.sizeNWSECursorSymbol, 2, -11, -11);
                        break;
                    case cursorSizeNE:
                    case cursorSizeSW:
                        currentCursorID = CursorManager.setCursor(SAP.instance.icons.sizeNESWCursorSymbol, 2, -11, -10);
                        break;
                    case cursorSizeN:
                    case cursorSizeS:
                        currentCursorID = CursorManager.setCursor(SAP.instance.icons.sizeNSCursorSymbol, 2, -10, -10);
                        break;
                }
            }
                        
            protected function titleBar_resizeMoveListener(event:MouseEvent):void
            {
                if (event.target is Button)
                {
                    //the base class doesn&apos;t give me access to "closeButton", so this
                    //is the only way to check if we are over the button
                    clearCursor();
                    return;
                }

                adjustCursor(event, true);
            }
            
            protected function resizeMoveListener(event:MouseEvent):void
            {
                //don&apos;t do it twice, the title bar takes care of it,
                //and don&apos;t do it if we aren&apos;t the the TitleWindow
                
                if (event.currentTarget.mouseY > getHeaderHeight()) //&& 
                    //event.target is SizeableTitleWindow)  
                {
                    
                    adjustCursor(event, false);
                }
            }
            
            override protected function startDragging(event:MouseEvent):void
            {
                // check for the threshholds first,  
                // if we are within the threshold do our stuff, else call super 
                var cursorStyle:int = getCursorStyle(event.currentTarget.mouseX, 
                    event.currentTarget.mouseY, true);
                if (cursorStyle != cursorSizeNone && cursorStyle != cursorSizeAll )
                {
                    startSizing(cursorStyle, event.stageX, event.stageY);
                } else {
                    super.startDragging(event);
                }
            }
            
            protected function resizeDownListener(event:MouseEvent):void
            {
                // check for the threshholds first, 
                // if we are within the threshold do our stuff, else call super 
                var cursorStyle:int = getCursorStyle(event.currentTarget.mouseX, 
                    event.currentTarget.mouseY, true);
                if (cursorStyle != cursorSizeNone && cursorStyle != cursorSizeAll)
                {
                    startSizing(cursorStyle, event.stageX, event.stageY);
                }                 
            }
            
            protected function startSizing(cursor:int, x:int, y:int):void
            {
                downX = x;
                downY = y;
                startHeight = this.height;
                startWidth = this.width;
                startLeft = this.x;
                startTop = this.y;
                resizeCursor = cursor;
                isResizing = true;                                
                                
                systemManager.addEventListener(
                    MouseEvent.MOUSE_MOVE, systemManager_resizeMouseMoveHandler, true, 0, true);
        
                systemManager.addEventListener(
                    MouseEvent.MOUSE_UP, systemManager_resizeMouseUpHandler, true, 0, true);
        
                stage.addEventListener(
                    Event.MOUSE_LEAVE, stage_resizeMouseLeaveHandler, false, 0, true);
                    
                this.dispatchEvent(new Event("startResize"));
            }            
    
            protected function stopSizing():void
            {
                isResizing = false;
                
                systemManager.removeEventListener(
                    MouseEvent.MOUSE_MOVE, systemManager_resizeMouseMoveHandler, true);
        
                systemManager.removeEventListener(
                    MouseEvent.MOUSE_UP, systemManager_resizeMouseUpHandler, true);
        
                stage.removeEventListener(
                    Event.MOUSE_LEAVE, stage_resizeMouseLeaveHandler);

                clearCursor();
                
                this.dispatchEvent(new Event("stopResize"));
            }
            
            private function sizeWidth(event:MouseEvent):void
            {
                var tmp:int;
                tmp = startWidth + event.stageX - downX;
                if (tmp >= minSizeWidth)   
                {
                    this.width = tmp;
                }                
            }

            private function sizeBottom(event:MouseEvent):void
            {
                var tmp:int;
                tmp = startHeight + event.stageY - downY;
                if (tmp >= minSizeHeight)   
                {                
                	sizeHeight(tmp);
                }                 	                
            }                        
                        
            private function sizeTop(event:MouseEvent):int
            {
                var tmp:int;
                var delta:int = downY - event.stageY; 
                tmp = startHeight + delta;
                if (tmp < minSizeHeight)   
                {                    
                    delta = minSizeHeight - startHeight;
                    tmp = startHeight + delta;                    
                }
                
                sizeHeight(tmp);
                return delta;
//                return 0;
            }
            
            private function sizeHeight(h:Number):void
            {                               
            	if(isNaN(explicitMaxHeight) || h <= maxHeight){
                	this.height = h;                    	
             	}
             	else{
             		this.height = maxHeight;
             	}
            }

            private function sizeLeft(event:MouseEvent):int
            {
                var tmp:int;
                var delta:int = downX - event.stageX;
                tmp = startWidth + delta;
                if (tmp < minSizeWidth)   
                {
                    delta = minSizeWidth - startWidth;
                    tmp = startWidth + delta;
                }
                
                this.width = tmp;
                return delta;
//                return 0;
            }

            /**
             *  @private
             */
            private function systemManager_resizeMouseMoveHandler(event:MouseEvent):void
            {
                var leftDelta:int=0;
                var topDelta:int=0;                
                switch (resizeCursor)
                {
                    case cursorSizeE:
                        sizeWidth(event);
                        break;
                    case cursorSizeSE:
                        sizeWidth(event);
                        sizeBottom(event);
                        break;                        
                    case cursorSizeS:
                        sizeBottom(event);
                        break;
                    case cursorSizeSW:
                        leftDelta = sizeLeft(event);
                        sizeBottom(event);
                        break;
                    case cursorSizeW:
                        leftDelta = sizeLeft(event);
                        break;
                    case cursorSizeNW:
                        topDelta = sizeTop(event);
                        leftDelta = sizeLeft(event);
                        break;
                    case cursorSizeN:
                        topDelta = sizeTop(event);
                        break;
                    case cursorSizeNE:
                        topDelta = sizeTop(event);
                        sizeWidth(event);
                        break;
                }
                
                // when sizing, we only want to do the move once (multiple moves cause ugly refresh problems)
                // a move happens when dragging involves the left or top side                
                if (leftDelta != 0 || topDelta != 0)
                {
                    move(startLeft - leftDelta, startTop - topDelta);
                }
            }
                 
            /**
             *  @private
             */
            private function systemManager_resizeMouseUpHandler(event:MouseEvent):void
            {
                stopSizing();
            }
        
            /**
             *  @private
             */
            private function stage_resizeMouseLeaveHandler(event:Event):void
            {
                stopSizing();
            }            
            
            private function cursorMouseOutListener(event:MouseEvent):void
            {
                if (!isResizing)
                {
                    clearCursor();
                }
            }
            
            [Bindable]
			override public function get verticalScrollBar():ScrollBar
		    {
		        return super.verticalScrollBar;
		    }
		    
		    override public function set verticalScrollBar(value:ScrollBar):void
		    {
		        super.verticalScrollBar = value;
		    } 
        
    }
}