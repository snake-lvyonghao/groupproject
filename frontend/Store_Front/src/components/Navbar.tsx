import { VStack } from "@chakra-ui/react";
import { Button } from "./ui/button";

interface props {
  onClick: (page: string) => void;
}

const Navbar = ({ onClick }: props) => {
  return (
    <VStack width="100%">
      {" "}
      <Button
        width="100%"
        height="50px"
        backgroundColor="#F8F8F8"
        color="black"
        _hover={{
          backgroundColor: "#E0E0E0",
        }}
        fontWeight="bold"
        borderRadius="md"
        onClick={() => {
          onClick("Menu");
        }}
      >
        Menu
      </Button>
      <Button
        width="100%"
        height="50px"
        backgroundColor="#F8F8F8"
        color="black"
        _hover={{
          backgroundColor: "#E0E0E0",
        }}
        fontWeight="bold"
        borderRadius="md"
        border="1px solid #D3D3D3"
        onClick={() => onClick("Cart")}
      >
        Cart
      </Button>
    </VStack>
  );
};

export default Navbar;
