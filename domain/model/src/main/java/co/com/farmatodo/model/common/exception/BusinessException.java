package co.com.farmatodo.model.common.exception;

import java.util.function.Supplier;

public class BusinessException extends ApplicationException {

    public enum Type {
        EMAIL_ALREADY_REGISTERED("Email is already registered"),
        PHONE_ALREADY_REGISTERED("Phone number is already registered"),
        TOKENIZATION_REJECTED_BY_PROBABILITY("Tokenization rejected by probability"),
        PRODUCT_NOT_FOUND("Product no found"),
        INSUFFICIENT_STOCK("Insufficient Stock"),
        CLIENT_NOT_FOUND("CLIENT_NOT_FOUND");





        private final String message;

        public String getMessage() {
            return message;
        }

        public BusinessException build() {
            return new BusinessException(this);
        }

        public Supplier<Throwable> defer() {
            return () -> new BusinessException(this);
        }

        Type(String message) {
            this.message = message;
        }
    }

    private final Type type;

    public BusinessException(Type type){
        super(type.getMessage());
        this.type = type;
    }

    @Override
    public String getCode(){
        return type.name();
    }
}
