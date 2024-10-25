import { Button, Card, Input, Stack } from "@chakra-ui/react";
import { Field } from "../components/ui/field";
import { PasswordInput } from "./ui/password-input";

const SignUp = () => {
  return (
    <Card.Root maxW="sm">
      <Card.Header>
        <Card.Title>Sign In</Card.Title>
      </Card.Header>
      <Card.Body>
        <Stack gap="4" w="full">
          <Field label="UserName">
            <Input />
          </Field>
          <Field label="Password">
            <PasswordInput />
          </Field>
        </Stack>
      </Card.Body>
      <Card.Footer justifyContent="flex-end">
        <Button variant="outline">Sign In</Button>
        <Button variant="solid">Sign Up</Button>
      </Card.Footer>
    </Card.Root>
  );
};

export default SignUp;
