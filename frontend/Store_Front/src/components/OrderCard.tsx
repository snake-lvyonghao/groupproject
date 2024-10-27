import { Box, Button, Card, HStack } from "@chakra-ui/react";
import { order } from "./OrderHistory";
import { OrderEndPoint } from "./services/EndPoints";
import {
  DialogActionTrigger,
  DialogBody,
  DialogCloseTrigger,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogRoot,
  DialogTitle,
  DialogTrigger,
} from "./ui/dialog";
import PostSender from "./RESTFul/PostSender";

interface props {
  customerId:number;
  order: order;
  setStatusCode: (code: number) => void;
  setMessage: (message: string) => void;
  setError: (error: string) => void;
}

const OrderCard = ({ order, setStatusCode, setMessage, setError }: props) => {
  const ENDPOINT = OrderEndPoint + "/" +"refund";


  const onClick = () => {
    PostSender(ENDPOINT, {orderId:order.id},setStatusCode, setMessage, setError);
  };

  return (
    <Card.Root width="90% " variant="elevated" key={order.id}>
      <Card.Body gap="2">
        <Card.Title mb="2">orderId: {order.id}</Card.Title>
        <Card.Description>
          <HStack spaceX={20} justifyContent="space-between">
            <Box
              minHeight="30px"
              maxHeight="30px"
              maxWidth="200px"
              color="balck"
              fontSize="lg"
            >
              Total Quantity: {order.totalQuantity}
            </Box>
            <Box
              minHeight="30px"
              maxHeight="30px"
              maxWidth="200px"
              color="balck"
              fontSize="lg"
            >
              OrderDate: {JSON.stringify(order.orderDate)}
            </Box>
           
            {/* <Button variant="surface" onClick={onClick}>
              Check Out
            </Button> */}
            <DialogRoot>
              <DialogTrigger asChild>
                <Button variant="outline" size="sm">
                  Cancel
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Confirm</DialogTitle>
                </DialogHeader>
                <DialogBody>
                  <p>Are you sure you wanna cancel?</p>
                </DialogBody>
                <DialogFooter>
                  <DialogActionTrigger asChild>
                    <Button variant="plain" onClick={onClick} bgColor="red">
                      cancel
                    </Button>
                  </DialogActionTrigger>
                </DialogFooter>
                <DialogCloseTrigger />
              </DialogContent>
            </DialogRoot>
          </HStack>
        </Card.Description>
      </Card.Body>
      <Card.Footer justifyContent="flex-end"></Card.Footer>
    </Card.Root>
  );
};

export default OrderCard;
