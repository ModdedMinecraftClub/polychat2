package club.moddedminecraft.polychat.core.messagelibrary;

import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.core.networklibrary.Message;
import com.google.protobuf.Any;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;

public final class PolychatProtobufMessageDispatcher {
    private final ArrayList<Object> eventHandlers = new ArrayList<>();

    public void addEventHandler(Object eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public void addEventHandlers(Object... eventHandlers) {
        this.eventHandlers.addAll(Arrays.asList(eventHandlers));
    }

    public void removeEventHandler(Object eventHandler) {
        eventHandlers.remove(eventHandler);
    }

    public void handlePolychatMessage(Message message) throws RuntimeException {
        try {
            Any packedProtoMessage = Any.parseFrom(message.getData());
            for (Object handler : eventHandlers) {
                Class<?> clazz = handler.getClass();
                for (Method method : clazz.getMethods()) {
                    if (isAcceptableEventHandler(method)) {
                        Parameter parameter = method.getParameters()[0]; //isAcceptableEventHandler verifies that there is exactly 1 argument
                        Class<?> parameterType = parameter.getType();
                        @SuppressWarnings("unchecked")
                        Class<? extends com.google.protobuf.Message> castedParameterType = (Class<? extends com.google.protobuf.Message>) parameterType; //this class is checked in isAcceptableEventHandler
                        if (packedProtoMessage.is(castedParameterType)) {
                            method.invoke(handler, packedProtoMessage.unpack(castedParameterType), message.getFrom());
                        }
                    }
                }
            }
        } catch (Throwable t) { //catch errors from event handlers + the parseFrom error
            throw new RuntimeException("Failed to parse/unpack/handle message.%s", t);
        }
    }

    private boolean isAcceptableEventHandler(Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }

        if (method.getParameterCount() != 2) {
            return false;
        }

        if(method.getParameters()[1].getType() != ConnectedClient.class){
            return false;
        }

        if (!com.google.protobuf.Message.class.isAssignableFrom(method.getParameters()[0].getType())) {
            return false;
        }

        if (method.getAnnotation(EventHandler.class) == null) {
            return false;
        }

        return true;
    }
}
